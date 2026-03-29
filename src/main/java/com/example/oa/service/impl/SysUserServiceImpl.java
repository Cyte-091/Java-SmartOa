package com.example.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa.auth.dto.request.ChangePwdRequest;
import com.example.oa.auth.dto.request.LoginRequest;
import com.example.oa.auth.dto.request.ResetPwdRequest;
import com.example.oa.auth.dto.request.UpdateStatusRequest;
import com.example.oa.auth.dto.response.LoginResponse;
import com.example.oa.auth.dto.response.ProfileResponse;
import com.example.oa.auth.dto.response.UserListResponse;
import com.example.oa.common.exception.UserException;
import com.example.oa.common.response.PageResult;
import com.example.oa.constants.user.ErrorEnum;
import com.example.oa.mapper.SysUserMapper;
import com.example.oa.model.SysRole;
import com.example.oa.model.SysUser;
import com.example.oa.model.SysUserRole;
import com.example.oa.service.SysRoleService;
import com.example.oa.service.SysUserRoleService;
import com.example.oa.service.SysUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    // 仅用于本地开发演示的 JWT 密钥。
    private static final String JWT_SECRET = "smart-oa-secret-2026";
    // Token 过期时间：2 小时。
    private static final long JWT_EXPIRE = 2 * 60 * 60 * 1000L;

    /**
     * 用户登录并签发 token。
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1) 基础参数校验：用户名、密码不能为空。
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "username and password are required");
        }

        // 2) 按用户名查询用户。
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername().trim());
        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);

        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "user not found");
        }

        // 3) 校验账号状态（0=禁用）。
        if (dbUser.getStatus() == null || dbUser.getStatus() == 0) {
            throw new UserException(ErrorEnum.USER_DISABLED, "user is disabled");
        }

        // 4) 校验密码。
        String inputPwd = request.getPassword().trim();
        if (dbUser.getPassword() == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "password not set");
        }
        String dbPwd = dbUser.getPassword().trim();
        if (!inputPwd.equals(dbPwd)) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "password incorrect");
        }

        // 5) 签发 token：写入用户 id + 密码版本摘要（用于改密后让旧 token 失效）。
        String token = Jwts.builder()
                .setSubject(dbUser.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE))
                .claim("pwdVer", buildPasswordVersion(dbPwd))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();

        // 6) 组装登录返回。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(dbUser, response);
        response.setToken(token);
        return response;
    }

    /**
     * 解析 Authorization 并返回当前登录用户。
     */
    @Override
    public LoginResponse getCurrentUser(String tokenHeader) {
        // 1) 校验请求头格式，必须是 Bearer token。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "invalid token header");
        }

        // 2) 解析 JWT。
        String token = tokenHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        // 3) 读取 claims：用户 id + 密码版本。
        String pwdVer = claims.get("pwdVer", String.class);
        Long userId = Long.valueOf(claims.getSubject());

        // 4) 查询数据库中的最新用户数据。
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new UserException(ErrorEnum.USER_DISABLED, "用户被禁用");
        }

        // 5) 对比密码版本；若已改密，则旧 token 视为失效。
        String expectedPwdVer = buildPasswordVersion(user.getPassword());
        if (pwdVer == null || !pwdVer.equals(expectedPwdVer)) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token invalidated by password change");
        }

        // 6) 返回当前用户基础信息。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    /**
     * 退出登录（当前无状态 JWT 模式下，主要做 token 合法性校验）。
     */
    @Override
    public void logout(String tokenHeader) {
        // 1) 校验请求头格式。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "invalid token header");
        }

        // 2) 校验 token 签名和过期时间。
        String token = tokenHeader.substring(7);
        try {
            Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token invalid");
        }
    }

    /**
     * 修改当前登录用户密码。
     */
    @Override
    public void changePassword(ChangePwdRequest request, String tokenHeader) {
        // 1) 先校验登录态。
        if (tokenHeader == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "invalid token header");
        }

        // 2) 解析当前用户。
        LoginResponse currentUser = getCurrentUser(tokenHeader);

        // 3) 校验新密码规则：非空、最小长度、确认密码一致。
        if (request.getNewPassword() == null
                || request.getNewPassword().length() < 6
                || !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "new password invalid");
        }

        // 4) 查询数据库中的最新用户记录。
        QueryWrapper<SysUser> userQw = new QueryWrapper<>();
        userQw.eq("id", currentUser.getId());
        SysUser dbUser = sysUserMapper.selectOne(userQw);

        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "user not found");
        }

        // 5) 校验旧密码，避免未授权修改。确保是本人操作，防止未授权用户恶意修改密码
        if (!dbUser.getPassword().equals(request.getOldPassword())) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "old password incorrect");
        }

        // 6) 更新为新密码。
        dbUser.setPassword(request.getNewPassword());
        sysUserMapper.updateById(dbUser);
    }

    /**
     * 生成密码版本摘要（用于 token 失效控制）。
     */
    private String buildPasswordVersion(String password) {
        // 密码为空时无法生成版本，按 token 无效处理。
        if (password == null) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token invalid");
        }

        // 拼接固定盐值，避免直接暴露原密码特征。
        String input = password + "::pwd-ver-salt::smart-oa";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token invalid");
        }
    }

    /**
     * 获取当前登录用户的个人信息（Profile）。
     * 说明：
     * 1. 复用统一的 token 解析与用户校验逻辑（getCurrentUser）。
     * 2. 仅返回前端个人中心需要的字段，避免把敏感信息直接暴露出去。
     */
    @Override
    public ProfileResponse getProfile(String tokenHeader) {
        if(tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token无效");
        }

        // 1) 通过统一鉴权入口获取当前用户，内部会完成 token 校验和用户状态检查。
        LoginResponse currentUser = getCurrentUser(tokenHeader);

        // 2) 组装 Profile 返回对象：只拷贝“个人信息页面”需要展示的字段。
        ProfileResponse response = new ProfileResponse();
        response.setId(currentUser.getId());
        response.setUsername(currentUser.getUsername());
        response.setDisplayName(currentUser.getDisplayName());
        response.setStatus(currentUser.getStatus());

        // 3) 返回给 controller，由 controller 统一封装 ApiResponse。
        return response;
    }

    /**
     * 管理员重置密码
     * @param id
     * @param request
     * @param tokenHeader
     */
    @Override
    public void resetPwd(Long id, ResetPwdRequest request, String tokenHeader) {
        // 1) 解析当前登录用户
        LoginResponse currentUser = sysUserService.getCurrentUser(tokenHeader);

        // 判断管理员
        if (!isAdmin(currentUser)) {
            throw new UserException(ErrorEnum.NOT_ADMIN, "非管理员，权限不足");
        }

        // 避免管理员误操作
        if (Objects.equals(id, currentUser.getId())) {
            throw new UserException("管理员不能重置自己密码");
        }



        //查找用户
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);
        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 6) {
            throw new UserException(ErrorEnum.PASSWORD_SHORT, "密码非法");
        }
        dbUser.setPassword(request.getNewPassword());
        sysUserMapper.updateById(dbUser);
    }

    /**
     * 管理员修改用户状态
     * @param id
     * @param request
     * @param tokenHeader
     */
    @Override
    public void updateStatus(Long id, UpdateStatusRequest request, String tokenHeader) {
        // 1) 解析当前登录用户
        LoginResponse currentUser = sysUserService.getCurrentUser(tokenHeader);

        // 判断管理员
        if (!isAdmin(currentUser)) {
            throw new UserException(ErrorEnum.NOT_ADMIN, "非管理员，权限不足");
        }

        // 避免管理员误操作
        if (Objects.equals(id, currentUser.getId())) {
            throw new UserException("管理员不能修改自己状态");
        }

        //查找用户
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);
        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 5) 状态校验
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new UserException(ErrorEnum.PARAM_ERROR, "用户状态非法");
        }

        // 6) 更新
        dbUser.setStatus(request.getStatus());
        sysUserMapper.updateById(dbUser);

    }

    /**
     * 获取用户列表
     * @param tokenHeader
     * @return
     */
    @Override
    public PageResult<UserListResponse> getUserList(Integer pageNum, Integer pageSize, String keyword, String tokenHeader) {
        // 校验当前用户是管理员
        LoginResponse currentUser = sysUserService.getCurrentUser(tokenHeader);
        if (!isAdmin(currentUser)) {
            throw new UserException(ErrorEnum.NOT_ADMIN, "非管理员，权限不足");
        }

        // 构建分页对象（核心！）
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        // 查询所有用户，并按id倒序
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        // 模糊查询的核心代码
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like("username", keyword).or().like("display_name", keyword));
        }

        queryWrapper.orderByDesc("id");

        // 分页查询
        sysUserMapper.selectPage(page, queryWrapper);

        // 封装成 DTO（不返回密码）
        List<UserListResponse> respList = new ArrayList<>();
        for (SysUser user :
                page.getRecords()) {
            UserListResponse resp = new UserListResponse();
            resp.setId(user.getId());
            resp.setUsername(user.getUsername());
            resp.setDisplayName(user.getDisplayName());
            resp.setStatus(user.getStatus());
            resp.setCreatedAt(user.getCreatedAt());
            respList.add(resp);
        }

        // 封装分页返回结果
        PageResult<UserListResponse> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(respList);

        return result;
    }

    /**
     * 判断用户权限是否为管理员
     * @param currentUser
     * @return
     */
    private boolean isAdmin(LoginResponse currentUser) {
        // 根据 user_id 查询用户-角色关系
        QueryWrapper<SysUserRole> userRoleQw = new QueryWrapper<>();
        userRoleQw.eq("user_id", currentUser.getId());
        List<SysUserRole> userRoleList = sysUserRoleService.list(userRoleQw);

        // 收集角色ID
        List<Long> roleIds = new ArrayList<>();
        for (SysUserRole userRole : userRoleList) {
            if (userRole.getRoleId() != null) {
                roleIds.add(userRole.getRoleId());
            }
        }

        if (roleIds.isEmpty()) return false;

        // 判断管理员
        // 判断是否有 ADMIN 角色
        List<SysRole> roles = sysRoleService.listByIds(roleIds);
        for (SysRole role : roles) {
            if ("ADMIN".equalsIgnoreCase(role.getRoleCode())) {
                return true;
            }
        }
        return false;
    }
}

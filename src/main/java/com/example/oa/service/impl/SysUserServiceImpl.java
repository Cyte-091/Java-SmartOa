package com.example.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa.auth.dto.ChangePwdRequest;
import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.auth.dto.ProfileResponse;
import com.example.oa.common.exception.UserException;
import com.example.oa.constants.user.ErrorEnum;
import com.example.oa.mapper.SysUserMapper;
import com.example.oa.model.SysUser;
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
import java.util.Date;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    // 开发环境示例密钥，生产环境请放到安全配置中心。
    private static final String JWT_SECRET = "smart-oa-secret-2026";
    // token 过期时间：2 小时（毫秒）。
    private static final long JWT_EXPIRE = 2 * 60 * 60 * 1000L;

    /**
     * 登录：校验用户身份并签发 token。
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1) 基础参数校验。
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "用户名和密码不能为空");
        }

        // 2) 按用户名查用户。
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername().trim());
        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);

        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 3) 校验用户状态（null/0 视为禁用）。
        if (dbUser.getStatus() == null || dbUser.getStatus() == 0) {
            throw new UserException(ErrorEnum.USER_DISABLED, "用户已禁用");
        }

        // 4) 校验密码（当前仍是明文比对，后续建议改 BCrypt）。
        String inputPwd = request.getPassword().trim();
        if (dbUser.getPassword() == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "密码未设置");
        }
        String dbPwd = dbUser.getPassword().trim();
        if (!inputPwd.equals(dbPwd)) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "密码错误");
        }

        // 5) 签发 token：subject 保存 userId，pwdVer 保存密码版本摘要。
        String token = Jwts.builder()
                .setSubject(dbUser.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE))
                .claim("pwdVer", buildPasswordVersion(dbPwd))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();

        // 6) 返回登录结果。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(dbUser, response);
        response.setToken(token);
        return response;
    }

    /**
     * 通过 Authorization 获取当前登录用户。
     */
    @Override
    public LoginResponse getCurrentUser(String tokenHeader) {
        // 1) 校验请求头格式。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "未登录或 token 格式错误");
        }

        // 2) 提取纯 token 并解析。
        String token = tokenHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        // 3) 从 token 中取 userId 与 pwdVer。
        String pwdVer = claims.get("pwdVer", String.class);
        Long userId = Long.valueOf(claims.getSubject());

        // 4) 查数据库用户。
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 5) 比对密码版本摘要，不一致说明密码已变更，旧 token 失效。
        String expectedPwdVer = buildPasswordVersion(user.getPassword());
        if (pwdVer == null || !pwdVer.equals(expectedPwdVer)) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token 不一致");
        }

        // 6) 返回用户基础信息。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    /**
     * 退出登录：当前阶段只校验 token 合法性。
     */
    @Override
    public void logout(String tokenHeader) {
        // 1) 校验请求头格式。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "未登录或 token 格式错误");
        }

        // 2) 解析 token，验证签名与过期。
        String token = tokenHeader.substring(7);
        try {
            Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token 不合法");
        }
    }

    /**
     * 修改密码：校验旧密码与新密码规则，成功后落库。
     */
    @Override
    public void changePassword(ChangePwdRequest request, String tokenHeader) {
        // 1) 校验登录态。
        if (tokenHeader == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "未登录或 token 格式错误");
        }

        // 2) 获取当前用户。
        LoginResponse currentUser = getCurrentUser(tokenHeader);

        // 3) 校验新密码规则。
        if (request.getNewPassword() == null
                || request.getNewPassword().length() < 6
                || !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "新密码不符合规范");
        }

        // 4) 读取数据库最新用户记录。
        QueryWrapper<SysUser> userQw = new QueryWrapper<>();
        userQw.eq("id", currentUser.getId());
        SysUser dbUser = sysUserMapper.selectOne(userQw);

        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 5) 校验旧密码。
        if (!dbUser.getPassword().equals(request.getOldPassword())) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "旧密码错误");
        }

        // 6) 更新新密码并落库。
        dbUser.setPassword(request.getNewPassword());
        sysUserMapper.updateById(dbUser);
    }

    @Override
    public ProfileResponse getProfile(String tokenHeader) {
        LoginResponse currentUser = getCurrentUser(tokenHeader);
        ProfileResponse response = new ProfileResponse();
        response.setId(currentUser.getId());
        response.setUsername(currentUser.getUsername());
        response.setDisplayName(currentUser.getDisplayName());
        response.setStatus(currentUser.getStatus());
        return response;
    }

    /**
     * 生成密码版本摘要（用于 token 失效控制）。
     *
     * 注意：这里返回摘要，不返回原始密码或密码片段。
     */
    private String buildPasswordVersion(String password) {
        if (password == null) {
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token 不合法");
        }

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
            throw new UserException(ErrorEnum.TOKEN_INVALID, "token 不合法");
        }
    }
}
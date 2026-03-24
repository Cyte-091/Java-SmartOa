package com.example.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
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
     * 登录接口核心逻辑。
     *
     * 处理流程：
     * 1) 校验参数
     * 2) 按用户名查用户
     * 3) 校验用户状态与密码
     * 4) 生成 JWT
     * 5) 返回脱敏后的登录信息（含 token）
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 第一步：基础参数判空，避免后续 trim/查询报错。
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "用户名和密码不能为空");
        }

        // 第二步：按用户名查库（这里默认用户名唯一）。
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername().trim());
        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);

        // 用户不存在，直接返回登录失败。
        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 账号状态检查：null 或 0 都视为不可登录。
        if (dbUser.getStatus() == null || dbUser.getStatus() == 0) {
            throw new UserException(ErrorEnum.USER_DISABLED, "用户已禁用");
        }

        // 第三步：密码比对（当前是明文比对，后续建议升级为 BCrypt）。
        String inputPwd = request.getPassword().trim();
        if (dbUser.getPassword() == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "密码未设置");
        }
        String dbPwd = dbUser.getPassword().trim();
        if (!inputPwd.equals(dbPwd)) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "密码错误");
        }

        // 第四步：生成 token，subject 存用户 id，后续 /me 通过它反查用户。
        String token = Jwts.builder()
                .setSubject(dbUser.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();

        // 第五步：组装返回体，token 单独塞入 response。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(dbUser, response);
        response.setToken(token);
        return response;
    }

    /**
     * 根据 Authorization 请求头获取当前登录用户。
     *
     * 要求请求头格式：Bearer <token>
     */
    @Override
    public LoginResponse getCurrentUser(String tokenHeader) {
        // 先校验请求头格式，防止 substring 越界。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "未登录或 token 格式错误");
        }

        // 去掉 "Bearer " 前缀，拿到纯 token 字符串。
        String token = tokenHeader.substring(7);

        // 解析 token：验签 + 验过期，失败会抛异常交给全局异常处理。
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        // 登录时把用户 id 放在 subject，这里取出来反查用户。
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 返回当前用户基础信息（不返回密码字段）。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
}
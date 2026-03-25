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
     * 登录核心流程。
     * 1) 参数校验
     * 2) 查用户并校验状态
     * 3) 校验密码
     * 4) 生成 token 并返回
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1) 判空，避免后续 trim 或查库报错。
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

        // 状态为 null 或 0 都视为禁用。
        if (dbUser.getStatus() == null || dbUser.getStatus() == 0) {
            throw new UserException(ErrorEnum.USER_DISABLED, "用户已禁用");
        }

        // 3) 密码比对（当前是明文比对，后续可升级 BCrypt）。
        String inputPwd = request.getPassword().trim();
        if (dbUser.getPassword() == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "密码未设置");
        }
        String dbPwd = dbUser.getPassword().trim();
        if (!inputPwd.equals(dbPwd)) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "密码错误");
        }

        // 4) 签发 token，subject 存 userId。
        String token = Jwts.builder()
                .setSubject(dbUser.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();

        // 返回登录结果。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(dbUser, response);
        response.setToken(token);
        return response;
    }

    /**
     * 根据 Authorization 请求头获取当前登录用户。
     */
    @Override
    public LoginResponse getCurrentUser(String tokenHeader) {
        // 1) 校验请求头格式。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "未登录或 token 格式错误");
        }

        // 2) 提取纯 token。
        String token = tokenHeader.substring(7);

        // 3) 解析 token，校验签名和过期时间。
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        // 4) 根据 subject(userId) 查用户。
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        // 5) 返回用户基础信息（不返回密码）。
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    /**
     * 退出登录。
     *
     * 当前阶段采用无状态 JWT：只校验 token 合法性。
     * 后续若要“立即失效”，需要接入 Redis 黑名单。
     */
    @Override
    public void logout(String tokenHeader) {
        // 1) 校验请求头格式。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "未登录或 token 格式错误");
        }

        // 2) 提取 token 并做解析校验。
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
}
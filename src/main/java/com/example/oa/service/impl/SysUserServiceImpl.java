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

    // 开发环境示例密钥，生产环境请放在安全配置中心。
    private static final String JWT_SECRET = "smart-oa-secret-2026";
    // token 过期时间：2小时（毫秒）
    private static final long JWT_EXPIRE = 2 * 60 * 60 * 1000L;

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "用户名和密码不能为空");
        }

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername().trim());

        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);
        if (dbUser == null) {
            throw new UserException(ErrorEnum.USER_NOT_FOUND, "用户不存在");
        }

        if (dbUser.getStatus() == null || dbUser.getStatus() == 0) {
            throw new UserException(ErrorEnum.USER_DISABLED, "用户已禁用");
        }

        String inputPwd = request.getPassword().trim();
        String dbPwd;
        if (dbUser.getPassword() == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR, "密码未设置");
        }

        dbPwd = dbUser.getPassword().trim();
        if (!inputPwd.equals(dbPwd)) {
            throw new UserException(ErrorEnum.PASSWORD_ERROR, "密码错误");
        }

        String token = Jwts.builder()
                .setSubject(dbUser.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();

        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(dbUser, response);
        response.setToken(token);
        return response;
    }
}
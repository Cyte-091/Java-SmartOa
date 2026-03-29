package com.example.oa.auth.controller;

import com.example.oa.auth.dto.request.ChangePwdRequest;
import com.example.oa.auth.dto.request.LoginRequest;
import com.example.oa.auth.dto.request.ResetPwdRequest;
import com.example.oa.auth.dto.request.UpdateStatusRequest;
import com.example.oa.auth.dto.response.LoginResponse;
import com.example.oa.auth.dto.response.ProfileResponse;
import com.example.oa.auth.dto.response.UserListResponse;
import com.example.oa.common.response.ApiResponse;
import com.example.oa.common.response.PageResult;
import com.example.oa.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户登录。
     *
     * 入参：用户名 + 密码。
     * 出参：用户基础信息 + token。
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1) 调用 service 执行登录校验并签发 token。
        LoginResponse response = sysUserService.login(request);
        // 2) 统一封装成功响应。
        return ApiResponse.success(response);
    }

    /**
     * 获取当前登录用户信息（用于前端显示当前账号状态）。
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@RequestHeader("Authorization") String token) {
        // 1) 由 service 解析 token 并返回当前用户。
        LoginResponse response = sysUserService.getCurrentUser(token);
        // 2) 返回给前端。
        return ApiResponse.success(response);
    }

    /**
     * 退出登录。
     *
     * 当前实现为无状态 JWT：后端主要做 token 合法性校验。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        // 1) 校验 token（过期/签名错误会抛异常）。
        sysUserService.logout(token);
        // 2) 返回空数据成功响应。
        return ApiResponse.success();
    }

    /**
     * 修改当前登录用户密码。
     */
    @PostMapping("/change-password")
    public ApiResponse<?> changePwd(@Valid @RequestBody ChangePwdRequest request,
                                       @RequestHeader("Authorization") String token) {
        // 1) 执行旧密码校验 + 新密码规则校验 + 更新数据库。
        sysUserService.changePassword(request, token);
        // 2) 返回成功。
        return ApiResponse.success("密码修改成功，请重新登录");
    }

    /**
     * 获取当前登录用户的个人资料。
     * 返回字段：id、username、displayName、status。
     */
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(@RequestHeader("Authorization") String token) {
        // 1) 调用 service 层读取 profile（内部会完成 token 校验）。
        ProfileResponse response = sysUserService.getProfile(token);
        // 2) 使用统一响应体返回给前端。
        return ApiResponse.success(response);
    }

    @PostMapping("/user/{id}/reset-password")
    public ApiResponse<?> resetPassword(@PathVariable Long id,
                                        @Valid @RequestBody ResetPwdRequest request,
                                        @RequestHeader("Authorization") String token) {
        sysUserService.resetPwd(id, request, token);
        return ApiResponse.success("密码重置成功");
    }

    @PostMapping("/user/{id}/status")
    public ApiResponse<?> status(@PathVariable Long id,
                                 @Valid @RequestBody UpdateStatusRequest request,
                                 @RequestHeader("Authorization") String token) {
        sysUserService.updateStatus(id, request, token);
        return ApiResponse.success("管理员修改用户状态成功");
    }

    @GetMapping("/user/list")
    public ApiResponse<PageResult<UserListResponse>> userList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestHeader("Authorization") String token) {
        PageResult<UserListResponse> response = sysUserService.getUserList(pageNum, pageSize, keyword, token);
        return ApiResponse.success(response);
    }

}
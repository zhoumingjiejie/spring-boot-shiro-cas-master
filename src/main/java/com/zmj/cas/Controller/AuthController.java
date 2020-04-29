package com.zmj.cas.Controller;

import com.zmj.cas.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 0.0.0
 * @ProjectName: [spring-boot-shiro-cas-master]
 * @Package: [com.zmj.cas.Controller.AuthController]
 * @Description 身份验证控制器
 * @Date 2020/3/28 22:08
 *
 * 首页：http://127.0.0.1:40301/shiro
 * 测试不用权限地址：http://127.0.0.1:40301/shiro/test
 * 测试使用权限地址：http://127.0.0.1:40301/shiro/testPermission
 * 测试权限不通过地址：http://127.0.0.1:40301/shiro/testPermissionFail
 * 测试登出地址：http://127.0.0.1:40301/shiro/logout
 */
@Slf4j
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/test")
    public String test() {
        return "请求成功！" + authService.getUsername();
    }

    @GetMapping("/testPermission")
    @RequiresPermissions("sys:dept:list")
    public String testPermission() {
        return "权限请求成功！" + authService.getUsername();
    }

    @GetMapping("/testPermissionFail")
    @RequiresPermissions("sys:dept:fail")
    public String testPermissionFail() {
        return "权限请求失败！" + authService.getUsername();
    }
}

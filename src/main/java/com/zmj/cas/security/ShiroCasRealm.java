package com.zmj.cas.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cas.CasRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 0.0.0
 * @ProjectName: [spring-boot-shiro-cas-master]
 * @Package: [com.zmj.cas.security.ShiroCasRealm]
 * @Description 身份、权限管理域
 * @Date 2020/3/28 19:09
 */
@Slf4j
public class ShiroCasRealm extends CasRealm {

    /**
     * 认证信息：在调用subject.login()时，首先调用此接口
     */
    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 调用父类的方法，然后授权用户
        AuthenticationInfo authc = super.doGetAuthenticationInfo(token);
        // 获得用户名
        String username = (String) authc.getPrincipals().getPrimaryPrincipal();
        // TODO:这里应该从数据库中获取用户信息
        return authc;
    }

    /**
     * 授权信息
     * 进行权限验证的时候，调用方法，将用户的权限信息写进SimpleAuthorizationInfo
     * 每次访问需授权资源时都会执行该方法中的逻辑，这表明本例中默认并未启用AuthorizationCache
     * 如果连续访问同一个URL（比如刷新），该方法不会被重复调用，Shiro有一个时间间隔，超过这个时间间隔再刷新页面，该方法会被执行
     */
    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 用户名称
        Object username = principals.getPrimaryPrincipal();
        //权限信息对象info,用来存放查出的用户的所有的角色（role）及权限（permission）
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // TODO: 这里应该从数据库获取用户权限（userDao.findByName(username);），可以根据实际情况做缓存，如果不做，Shiro自己也是有时间间隔机制，2分钟内不会重复执行该方法
        Set<String> permission = new HashSet<>();
        permission.add("sys:dept:list");
        info.setStringPermissions(permission);
        return info;
    }
}

package com.zmj.cas.service.impl;

import com.zmj.cas.service.AuthService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

/**
 * @author 0.0.0
 * @ProjectName: [spring-boot-shiro-cas-master]
 * @Package: [com.zmj.cas.service.impl.AuthServiceImpl]
 * @Description 身份验证实现类
 * @Date 2020/3/28 22:06
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public String getUsername() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || subject.getPrincipals() == null) {
            return null;
        }
        return (String) subject.getPrincipals().getPrimaryPrincipal();
    }
}

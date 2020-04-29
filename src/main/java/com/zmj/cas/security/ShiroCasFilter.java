package com.zmj.cas.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.cas.CasFilter;
import org.apache.shiro.cas.CasToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 0.0.0
 * @ProjectName: [spring-boot-shiro-cas-master]
 * @Package: [com.zmj.cas.security.ShiroCasFilter]
 * @Description cas过滤器
 * @Date 2020/3/28 19:40
 */
@Slf4j
public class ShiroCasFilter extends CasFilter {

    private static final String TICKET_PARAMETER = "ticket";

    public ShiroCasFilter() {
    }

    /**
     * 重写创建ticket方法，支持从参数或头部获取ticket
     */
    @Override
    public AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        // 获取请求的ticket
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ticket = getRequestTicket(httpRequest);
        if (StringUtils.isEmpty(ticket)) {
            log.debug("票证获取失败,票证为空！");
            return null;
        }
        return new CasToken(ticket);
    }

    /**
     * 允许访问
     * 拒绝除了option以外的所有请求
     **/
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return ((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name());
    }

    /**
     * 重写拒绝访问方法，支持从参数或头部获取ticket
     */
    @Override
    public boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // 获取ticket，如果不存在，直接返回false
        String ticket = getRequestTicket((HttpServletRequest) request);
        if (StringUtils.isEmpty(ticket)) {
            return false;
        }
        return this.executeLogin(request, response);
    }

    /**
     * 从参数中获取ticket,如果为空的话，则从header中获取参数
     */
    private String getRequestTicket(HttpServletRequest httpRequest) {
        // 从参数中获取ticket
        String ticket = httpRequest.getParameter(TICKET_PARAMETER);
        if (StringUtils.isEmpty(ticket)) {
            // 如果为空的话，则从header中获取参数
            ticket = httpRequest.getHeader(TICKET_PARAMETER);
        }
        return ticket;
    }
}

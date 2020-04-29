package com.zmj.cas.config;

import com.zmj.cas.security.ShiroCasFilter;
import com.zmj.cas.security.ShiroCasRealm;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.cas.CasSubjectFactory;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 0.0.0
 * @ProjectName: [spring-boot-shiro-cas-master]
 * @Package: [com.zmj.cas.config.ShiroCasConfig]
 * @Description shiro+cas单点登录配置文件
 * @Date 2020/3/28 18:22
 */
@Slf4j
@Configuration
public class ShiroCasConfig {

    /**
     * 【解析】LifecycleBeanPostProcessor：处理bean生命周期（初始化，销毁）
     * 保证了shiro内部lifecycle函数bean的执行
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 解析：DefaultAdvisorAutoProxyCreator：实现了BeanProcessor接口,普通bean执行前执行此方法，当ApplicationContext读如所有的Bean配置信息后，这个类将扫描上下文，寻找所有的Advisor，
     * 将这些Advisor应用到所有符合切入点的Bean中（配置bean包，可正则表达式）。如：这些bean调用方法时，都会经过MethodBeforeAdvice方法实现类
     * <p>
     * 开启Shiro的注解(如@RequiresPermissions)
     * 需借助SpringAOP扫描使用Shiro注解的类
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /**
     * 【解析】ModularRealmAuthenticator： 模块化域身份验证,源码使用realm调用doGetAuthenticationInfo方法
     * 配置授权策略
     */
    @Bean
    public ModularRealmAuthenticator modularRealmAuthenticator() {
        ModularRealmAuthenticator modularRealmAuthenticator = new ModularRealmAuthenticator();
        modularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        return modularRealmAuthenticator;
    }

    @Bean("casRealm")
    public ShiroCasRealm casRealm(@Value("${shiro.casServerUrlPrefix}") String casServerUrlPrefix,
                                  @Value("${shiro.shiroServerUrlPrefix}") String shiroServerUrlPrefix,
                                  @Value("${shiro.casFilterUrlPattern}") String casFilterUrlPattern) {
        ShiroCasRealm casRealm = new ShiroCasRealm();
        // 认证通过后的默认角色
        casRealm.setDefaultRoles("ROLE_USER");
        // cas服务端地址前缀 http://127.0.0.1:8080/cas
        casRealm.setCasServerUrlPrefix(casServerUrlPrefix);
        // 应用服务地址，用来接收cas服务端票证：http://127.0.0.1:40301/shiro/shiro-cas
        casRealm.setCasService(shiroServerUrlPrefix + casFilterUrlPattern);
        return casRealm;
    }


    /**
     * 配置安全管理器
     **/
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(ModularRealmAuthenticator modularRealmAuthenticator,
                                                               ShiroCasRealm casRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置授权策略,此步骤必须在设置realm的前面，不然会报错realm未配置
        securityManager.setAuthenticator(modularRealmAuthenticator);
        // 指定 SubjectFactory，封装AuthenticationToken token、isRememberMe、Session的管理
        securityManager.setSubjectFactory(new CasSubjectFactory());
        // 缓存管理器,<!-- 用户授权/认证信息Cache, 采用MemoryConstrainedCacheManager 缓存,可以修改为EhCache、redis -->
        securityManager.setCacheManager(new MemoryConstrainedCacheManager());
        // 设置自定义验证策略（里面包括cas认证完后返回的ticket）
        securityManager.setRealm(casRealm);
        return securityManager;
    }

    /**
     * 配置cas登录过滤器
     */
    @Bean(name = "casFilter")
    public ShiroCasFilter shiroCasFilter(@Value("${shiro.loginUrl}") String loginUrl) {
        ShiroCasFilter casFilter = new ShiroCasFilter();
        casFilter.setName("casFilter");
        casFilter.setEnabled(true);
        // 登录失败后跳转的URL，也就是 Shiro 执行 MyCasRealm 的 doGetAuthenticationInfo 方法向CasServer验证tiket
        casFilter.setFailureUrl(loginUrl);
        return casFilter;
    }

    /**
     * shiro过滤器
     *
     * @param securityManager 安全管理器
     * @param casFilter       cas登录过滤器
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, ShiroCasFilter casFilter,
                                                         @Value("${shiro.logoutUrl}") String logoutUrl,
                                                         @Value("${shiro.loginUrl}") String loginUrl,
                                                         @Value("${shiro.casFilterUrlPattern}") String casFilterUrlPattern) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 设置登录地址
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        // 设置登录成功地址
        shiroFilterFactoryBean.setSuccessUrl("/");
        // 配置拦截地址
        Map<String, Filter> filters = new HashMap<>();
        filters.put("casFilter", casFilter);
        LogoutFilter logoutFilter = new LogoutFilter();
        // 配置登出地址
        logoutFilter.setRedirectUrl(logoutUrl);
        filters.put("logout", logoutFilter);
        shiroFilterFactoryBean.setFilters(filters);
        // 设置访问用户页面需要授权的操作
        loadShiroFilterChain(shiroFilterFactoryBean, casFilterUrlPattern);
        // 将设置的权限设置到shiroFilterFactoryBean
        return shiroFilterFactoryBean;
    }

    /**
     * shiro开启aop注解支持
     * <p>
     * Controller方法上需求权限注解
     * 列子：@RequiresPermissions("userInfo:test")
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 添加shiro的filter
     */
    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> filterRegistrationBean() {
        FilterRegistrationBean<DelegatingFilterProxy> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new DelegatingFilterProxy("shiroFilter"));
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");
        //  该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        filterRegistrationBean.addInitParameter("targetFilterLifecycle", "true");
        return filterRegistrationBean;
    }

    /**
     * 1、当我们第一次访问客户端时，先去cas进行认证，成功后会返回一个ticket
     * 2、返回的ticket地址在casRealm已经进行了配置，shiroServerUrlPrefix + casFilterUrlPattern
     * 3、即地址为/shiro-cas，对该地址进行casFilter拦截
     */
    private void loadShiroFilterChain(ShiroFilterFactoryBean shiroFilterFactoryBean, String casFilterUrlPattern) {
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        //拦截csa重定向http://127.0.0.1:40301/shiro/shiro-cas请求。交由MyCasFilter处理，获取返回的ticket。处理完后调用MyCasRealm认证处理
        filterChainDefinitionMap.put(casFilterUrlPattern, "casFilter");
        filterChainDefinitionMap.put("/logout", "logout");

        //filterChainDefinitionMap.put("/testPermissionFail", "authc,perms[sys:dept:fail]");
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    }
}

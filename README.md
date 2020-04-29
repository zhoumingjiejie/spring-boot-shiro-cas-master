 # 环境要求
 参考CAS server打包：https://www.cnblogs.com/zeussbook/p/10443560.html
 
 #使用
 1. spring-boot-shiro-cas-master\cas-overlay-template-5.2\src\main\resources\application.properties 配置好数据库打包
 2. 部署好cas.war(上面打包好的war)进入tomcat
 3. 客户端application.yml声明cas访问地址casServerUrlPrefix: http://127.0.0.1:8080/cas
 
 ##测试访问地址
 * 默认账号/密码：user/123456
 * 首页：http://127.0.0.1:40301/shiro
 * 测试不用权限地址：http://127.0.0.1:40301/shiro/test
 * 测试使用权限地址：http://127.0.0.1:40301/shiro/testPermission
 * 测试权限不通过地址：http://127.0.0.1:40301/shiro/testPermissionFail
 * 测试登出地址：http://127.0.0.1:40301/shiro/logout
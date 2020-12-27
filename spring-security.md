## spring-security 安全框架



#### 用户认证

三个接口：

`UserDetailsServer`：用户登录验证接口，实现其中`loadUserByUsername(String username)`方法，执行登录校验

`UserDetails`：**用户信息实体类**需要继承的接口，

`PasswordEncoder`：密码加密接口，常用`BCryptPasswordEncoder`






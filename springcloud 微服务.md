# 服务治理-Eureka



Eureka 作为服务注册中心，系统中的其他微服务，使用 Eureka 的客户端连接到 Eureka Server 并**维持心跳连接**。

```yml
eureka:
	instance:
		hostname: localhost
	client:
		# false 表示不注册自己
		register-with-eureka: false
		# false 表示自己是注册中心，不需要检索服务 
		fetch-registry: false
		service-url:
			defaultZone: http://$()/...
```

@Enable
























# 第一部分 Spring 

## Spring IoC

Spring 框架的核心是 Spring IoC 容器。容器创建 Bean 对象，将它们装配在一起，配置它们并管理它们的完整生命周期。

#### BeanFactory 和 ApplicationContext

Spring 提供了两个接口来表示容器，BeanFactory 简单理解为 HashMap，提供 put 和 get 方法，是低级容器

ApplicationContext 是高级容器，有更多功能，提供 refresh 方法用于刷新整个容器，重新加载/刷新 Bean。

ApplicationContext 依赖 BeanFactory 的 getBean 功能。BeanFactory 只负责加载 Bean，获取 Bean，

#### 实现机制

简单来说，Spring 中的 IoC 的实现原理，就是**工厂模式**加**反射机制**。代码如下：

```java
interface Fruit {
     public abstract void eat();
}

class Apple implements Fruit {
    public void eat(){
        System.out.println("Apple");
    }
}

class Orange implements Fruit {
    public void eat(){
        System.out.println("Orange");
    }
}

class Factory {
    public static Fruit getInstance(String className) {
        Fruit f = null;
        try {
            f = (Fruit) Class.forName(className).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }
    
}

class Client {
    public static void main(String[] args) {
        Fruit f = Factory.getInstance("io.github.dunwu.spring.Apple");
        if(f != null){
            f.eat();
        }
    }
}
```

- Fruit 接口，有 Apple 和 Orange 两个实现类。
- Factory 工厂，通过反射机制，创建 `className` 对应的 Fruit 对象。
- Client 通过 Factory 工厂，获得对应的 Fruit 对象。
- 😈 实际情况下，Spring IoC 比这个复杂很多很多，例如单例 Bean 对象，Bean 的属性注入，相互依赖的 Bean 的处理，以及等等。

## Spring Bean

由 Spring IoC 容器实例化，配置，装配和管理的

#### 配置方式：

1. XML 配置文件，一般用于 Dubbo 服务的配置
2. 注解配置，使用 `@RequestMapping` 注解配置 Spring MVC 请求
3. Java Config，Spring MVC 拦截器的配置

#### Bean Scope

了解 Singleton 和 Prototype 两种级别即可

### Bean 在容器的生命周期

<u>**实例化**和**属性赋值**对应构造方法和setter方法的注入，**初始化**和**销毁**是用户能自定义扩展的两个阶段。</u>

##### 构造（对象创建）

createBeanInstance()

​		单实例：在容器启动的时候创建对象

​		多实例：在每次获取的时候创建对象

##### 属性赋值

populateBean()

​		对应 setter 方法的注入

##### 初始化

initializeBean

​		对象创建完成，并赋值好，调用初始化方法

##### 销毁

​		单实例：容器关闭的时候

​		多实例：容器不会管理这个 bean，容器不会进行销毁



## Spring AOP

Spring AOP ，首先 aop 可以通过静态代理或者动态代理来实现的，Spring AOP 使用了动态代理+反射来实现。

同时 Spring 的 AOP 会根据代理的类是否实现了接口来选择使用 JDK 的动态代理还是 cglib 的动态代理。

jdk 的动态代理需要类实现接口是因为它是通过反射机制生成一个代理接口的匿名类，继承 Proxy 类来实现代理操作的，Java 因为是单继承，所以通过实现那个接口来达到方法调用时增强的行为。调用具体方法时调用 invokeHandler

cglib 是通过直接继承的方式。通过修改字节码生成子类去处理

#### Aspectj 

**Spring AOP** 基于**动态代理**

**Aspectj AOP** 基于**静态代理**

## Spring 事务（Transaction）

#### 事务是什么

对一系列的数据库操作进行统一的提交或回滚。如果多条数据，就是插入成功，必然多条都成功，一条失败，就全都失败。==可以防止脏数据出现。==

#### 事务特性 ACID

A：原子性（atomic）

C：一致性（Consistency ）

I：隔离性 Isolation

D：持久性 Durability

## Spring 有哪些类型的事件

Spring 的 ApplicationContext 提供了支持事件和代码中监听器的功能。

Bean 可以通过实现 ApplicationListener 接口来获取 ApplicationEvent 发布的事件。

Spring 提供5种标准事件：

1. 上下文更新事件（ContextRefreshedEvent）：该事件会在ApplicationContext 被初始化或者更新时发布。也可以在调用ConfigurableApplicationContext 接口中的 `#refresh()` 方法时被触发。
2. 上下文开始事件（ContextStartedEvent）：当容器调用ConfigurableApplicationContext 的 `#start()` 方法开始/重新开始容器时触发该事件。
3. 上下文停止事件（ContextStoppedEvent）：当容器调用 ConfigurableApplicationContext 的 `#stop()` 方法停止容器时触发该事件。
4. 上下文关闭事件（ContextClosedEvent）：当ApplicationContext 被关闭时触发该事件。容器被关闭时，其管理的所有单例 Bean 都被销毁。
5. 请求处理事件（RequestHandledEvent）：在 We b应用中，当一个HTTP 请求（request）结束触发该事件。

还可以继承 ApplicationEvent 类来扩展自定义事件






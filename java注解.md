# Java 注解

## 注解概述

#### 注解分类

源码注解

编译注解
		`@Override`，`@Deprecated`，`@Suppvisewarnings`

运行时注解
		`@Autowired`

## 自定义注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME) //表明是运行时注解
@Inherited
@Documented
public @interface Description { //定义注解
    String desc(); //成员以无参无异常方式声明

    String author();

    int age() default 18; //可以用 default 为成员指定默认值
}
```

- 成员类型是受限的，合法的类型包括**基本数据类型**及 **String, Class, Annotation, Enumeration**
- 如果注解只有一个成员，则成员名必须**取名为 `value()`**，在使用时可忽略成员名和赋值号（=）`@Value("haha")`
- 注解类可以没有成员，没有成员的注解称为**标识注解**

#### `@Target`

target 注解是为了标明这个注解可使用的地方

#### `@Retention`

标识注解的存在范围

#### `@Inherited`

标明可被继承，继承只在 class 上有效，interface 和 method 上都不继承

## 项目实战

#### 项目场景

使用注解来替代 Hibernate 中的一些功能








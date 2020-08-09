# scala 语言基础

### 函数式编程

纯函数，或函数的纯粹性，没有副作用

##### ex：

1. 修改全局变量
2. 抛出异常
3. IO 读写
4. 调用有副作用的函数

##### 副作用

无副作用：`def xplusy_1() = x + y`
有副作用：`def xplusy_2() = {x += y; x}`

##### 引用透明

对于相同的输入，总是得到相同的输出

违反引用透明：
		`StringBuffer` 的 `append` 方法

## 表达式求值策略

严格求值： Call By Value vs. Call By Name



## 环境安装

jdk 1.8 以上

scala 2.11

sbt：simple build tool

REPL：Read Evaluate Print Loop（交互式的程序运行环境）

## 基础语法

#### 变量

val 定义 immutable variable（不可变）

var 定义 mutable variable（可变）

lazy val：在第一次使用时才进行赋值，一般定义在不确定是否使用的量上
ex：`lazy val x = e * s;`

#### 数据类型

String

构建于 Java 的 String 之上，新增了字符串插值（interpolation） 的特性

```scala
val myname = "jett" // create a String
s"My name is ${myname}" // String interpolation
```

#### Block（代码块）

```scala
// 第一种写法
{ exp1; exp2 }

// 第二种写法
{
    exp1
    exp2
}
```

#### 函数

```scala
def functionName( param: ParamType): Return Type = {
	function body: expressions
}
```

#### if 与 for

```scala
for{
    
} xxx
```

#### try 与 match



## 高阶函数

1. 函数作为参数
2. 函数作为返回值

#### 匿名函数

匿名函数表面上是没有命名的函数，本质上是一个字面量（常量），即不是变量，没有一个变量名与之对应

scala 中，匿名函数的定义格式：
				`(形参列表) => {函数体}`

#### 柯里化





## 闭包














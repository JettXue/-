## 第三章 线程池

#### 线程创建时机

线程维护核心线程数量，当达到核心数量时，再添加进来的任务会进入队列中等待，队列满了之后，再有任务会从队列头开始创建新的线程，当创建的线程超过最大线程数时，进行拒绝



#### 队列类型

1. 直接交接：SynchronousQueue，实际队列没有容量，只是做一下中转
2. 无界队列：LinkedBlockingQueue，不会创建新的线程，可以防止流量突增，但是如果监控不得当，会造成 oom
3. 有界队列：ArrayBlockingQueue，常用，队列容量满了就创建新的线程



#### Executor 常见线程池

* FixedThreadPool：固定线程数量，可能差生 oom
* SingleThreadExecutor：同上，只产生1个线程
* CacheThreadPool：无限线程，直接执行
* ScheduledThreadPool：定时，周期性
  * **.schedule(command, delay, unit);
  * **.scheduleAtFixedRate(command,  initialDelay, period, unit)



#### 线程数量设定

* CPU 密集型（加密、计算hash）：最佳为 CPU 核心数的 1-2 倍
* 耗时 IO 型（读写数据库、文件、网络读写）：一般大于核心很多倍，根据监控的繁忙为依据，保证线程空闲可衔接
* 线程数 = CPU 核心数 * （1 + 平均等待时间 / 平均工作时间）

--> 根据压测来判断是否添加或减少线程数



#### 拒绝策略

###### 拒绝时机

1. 当 Executor 已经关闭，新任务会被拒绝
2. Executor 对<u>最大线程和工作队列容量</u>使用**有限边界**并且**已经饱和**

###### 4种拒绝策略

* AbortPolicy：直接抛出异常
* DiscardPolicy：放弃策略
* DiscardOldestPolicy：丢弃最老的策略
* CallerRunsPolicy：任务提交者执行任务

#### 线程池中的线程

线程池中的线程会循环检查是否有新的任务，当有新的任务进入时会执行，完成后继续循环。

```java
try {
    while (task != null || (task = getTask()) != null) {
        // do something
    }
    completedAbruptly = false;
} finally {
    processWorkerExit(w, completedAbruptly);
}
```

#### 线程池状态

* running：接受新任务并处理排队任务
* shutdown：不接受新任务，但处理排队任务
* stop：不接受新的任务，中断正在进行的任务
* tidying：所有任务终止后进入
* terminated： terminate() 运行完成

#### execute() 方法执行过程

1. 获取工作线程数并判断是否小于核心线程
2. 如果 addWorker() 执行成功，返回
3. 再次判断是否运行，并添加到工作队列
4. 如果不是正在运行则执行 reject()
5. 如果线程因异常而没有了，则添加线程执行
6. 如果核心到了，队列也满了，开始增加线程执行任务
7. 直到线程数满了，再进入的任务将 reject()



## 第四章 ThreadLocal

#### ThreadLocal 使用场景

1. 每个线程需要一个独享对象（通常是**工具类**，典型如 SimpleDateFormat 和 Random）
2. 每个线程内需要**保存全局变量**（例如拦截器中获取用户信息，<u>每个请求都直接将第一个参数直接写入全局变量</u>），可以让不同方法直接使用，避免**参数传递麻烦**

##### 第一种  独享对象（SimpleDateFormat）

1. 只有几个线程，直接使用
2. 很多线程，需要使用**线程池**，否则耗内存
3. 线程*共用一个 SimpleDateFormat 对象*
4. *线程不安全*
5. **加锁**，*效率低*
6. 最终使用 **ThreadLocal**，通过 initialValue 设置



##### 第二种  保存全局变量（拦截器）

在第一个位置 set 相应变量之后，后面的 service 可以直接读取

> 此情景的另外一种解决方法：
>
> 可以用 static 的 ConcurrentHashMap，吧当前的线程 ID 作为 key，把 user 作为 value 来保存，可以做到线程隔离，但是仍然影响性能



#### ThreadLocal 优点总结

- 线程安全
- 执行效率高
- 内存占用低
    每个线程只需一个，即使执行不同的 Task，使用的也是同一份共享对象副本，这是线程独有的
- 不用传参，代码耦合降低

#### ThreadLocal 原理

> <u>**每个线程 Thread 持有一个 ThreadLocalMap 变量，里面用来保存对应的 ThreadLocal 对象**</u>

##### 重要方法

- **initialValue**()

延迟加载的方法，当调用 get 方法时，会调用这个方法，如果 set 过，则会返回 set 的值

如果 remove，则里面的对象会清空

一般使用匿名内部类的方法**重写 initialValue()**

- **set**()

设置对象

- **get**()

获取对应的 value，首次使用会调用 initialValue

- **remove**

清空对象

#### 防止内存泄漏

> 内存泄漏：某个对象不再有用，但是占有的内存无法回收


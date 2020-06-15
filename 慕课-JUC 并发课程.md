## 第三章 线程池

> 7大参数；满了，怎么办；再满，怎么办

#### 7大参数

1. **corePoolSize**：线程池中常驻核心线程数
2. **maximumPoolSize**：线程池能够容纳同时执行的最大线程数，此值必须大于等于1
3. **keepAliveTime**：多余的空闲线程存活时间。当前线程池数量超过corePoolSize时，当空闲时间到达keepAliveTime值时，多余空闲线程会被销毁直到只剩下corePoolSize个线程为止。
4. unit：keepAliveTime的时间单位
5. **workQueue**：任务队列，被提交但尚未执行的任务
6. **threadFactory**：表示生成线程池中的工作线程的线程工厂，用于创建线程，一般为默认线程工厂即可
7. **handler**：拒绝策略，表示当队列满了并且工作线程大于等于线程池的最大线程数（maximumPoolSize）时如何来拒绝来请求的Runnable的策略

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

key 使用了弱引用进行创建

> 弱引用：如果对象只被弱引用关联，那么这个对象可以被回收
>
> 弱引用不会阻止 GC

ThreadLocal 存在以下调用链

> Thread ——> ThreadLocalMap ——> Entry（key 为 null） ——> Value

value 是强引用，无法被回收

ThreadLocal 使用基本类型的封装类型，如果返回值使用的基本类型，并且没有设置初始值，可能导致**装箱拆箱**中**类型转换的空指针问题**。

#### Spring 使用 ThreadLocal

- DateTimeContextHolder
- RequestContextHolder
- ContextHolder



## 第五章 锁

#### 锁分类

- 乐观锁|悲观锁
- 可重入锁|不可重入锁（**ReentrantLock**）
- 公平锁|非公平锁
- 共享锁|排它锁（**ReentrantReadWriteLock**）
- 自旋锁|阻塞锁
- 可中断锁

#### Lock 接口

##### synchronized 存在的缺点

- **效率低**：锁释放少，试图获取锁时不能设定超时，无法中断试图获得锁的线程
- **不够灵活**：加锁、释放的时机单一，每个锁仅有单一条件
- 无法知道是否**成功获取到锁**
- 锁升级后**无法降级**

##### Lock 常用方法

###### lock()

普通的获取锁，无法获取则**等待**，**异常时不会释放锁**，最佳实践是**在 finally 中释放锁**。lock() 方法无法被中断，一旦**死锁**，就会永久等待

###### tryLock()

尝试获取锁，并**立即返回**，

###### tryLock(long time, TimeUnit unit)

**超时就放弃**

###### lockInterruptibly()

超时时间为无限，在等锁过程中**可被中断**

#### 锁的可见性

> happens-before 原则：一把锁在解锁前的所有操作，对于下一个锁是完全可见的

#### 锁分类2

- 是否锁住同步资源
  - 锁住：悲观锁
  - 不锁：乐观锁
- 多线程**是否共享**一把锁
  - 共享：共享锁
  - 不共享：独占锁
- 多线程竞争时，是否排队
  - 排队：公平锁
  - 尝试插队，插队失败再排队：非公平锁
- 同一个线程是否能重复获取一把锁
  - 可以：可重入锁
  - 不可以：不可重入锁
- 可否中断
  - 可以：可中断锁
  - 不可以：非可中断锁
- 等锁过程
  - 自旋：自旋锁
  - 阻塞：非自旋锁

#### 公平/非公平锁

- 公平：完全按照线程请求顺序来分配锁

- 非公平：不完全按顺序，一定情况可插队

> 非公平不提倡插队，而是在合适时机插队

##### 合适的时机

在队列首位的线程处于阻塞等待状态，而唤醒需要一定开销，此时可以安排正在运行的线程先进行执行，可以提高效率，**避免唤醒带来的空档期**

**tryLock() 方法可以直接插队，不管公平锁**

#### 共享锁

读写分离，读互不影响，可以共存，写只能同时一个写

两种插队策略：

- 读可以一直插队，可能造成写操作饥饿
- 读无法插写的队，损失一点效率，但是避免了饥饿
  - 读可以在队列第一个不是写操作的时候插队，直到队列第一个是写操作之后，就不能再插队了

#### 读写锁的锁升降级

**支持降级**，当写锁使用完后需要读操作，但是不想让人插队，就可以降级，开始读操作，也不影响其他线程进行读操作

**不支持升级**，读写锁由于不能同时写，但能同时读，所以当多个线程在读的时候，如果都想升级，但是需要其他线程放弃读锁，就会造成相互等待释放读锁，却无法升级成写锁的情况

#### 自旋锁和阻塞锁

- 阻塞锁
  - 线程如果得不到执行，就进入阻塞状态，等能执行时，进行唤醒
  - 进行唤醒将消耗较多的 cpu 资源，但是可能执行的操作并不多，就浪费了 cpu 资源
- 自旋锁
  - 在锁很快被释放的情况下，使用自旋去等待释放，当锁释放后可以立即得到执行，不必消耗切换线程的资源
  - 如果锁占用时间很长，将造成**资源消耗线性增长**

#### 如何通过锁的使用提升并发性能

##### jvm 锁优化

- 锁升级
- 锁消除
- 锁粗化

##### 代码优化

- 缩小同步代码块
- 尽量不锁方法
- 减少请求锁的次数
- 避免人为制造“热点”
- 锁尽量不包含锁
- 选择合适的锁类型或合适的工具类



## 第六章 atomic 包

#### 原子类

- 不可分割
- 操作不可中断
- 比锁粒度更细

##### 六种原子类

| 类名称              | 类型     | 举例                                         |
| ------------------- | -------- | -------------------------------------------- |
| Atomic*             | 基本类型 | AtomicInteger<br />AtomicLong                |
| Atomic*Array        | 数组类型 | AtomicIntegerArray<br />AtomicLongArray      |
| Atomic*Reference    | 引用类型 | AtomicReference<br />AtomicStampedeReference |
| Atomic*FieldUpdater | 升级类型 | 。。。                                       |
| Adder               | 累加器   | LongAdder、DoubleAdder                       |
| Accumulator         | 累加器   | LongAccumulator、DoubleAccumulator           |



## 第七章 CAS 原理

#### cas 是什么

> CAS：Compare And Swap，比较并交换

在 AtomicInteger 类中，调用 getAndIncrement 方法进行 CAS 操作

```java
public final int getAndIncrement() {
    return unsafe.getAndAddInt(this, valueOffset, 1);
}
```

使用了 Unsafe 类的 getAndAddInt 方法

```java
public final int getAndAddInt(Object var1, long var2, int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1, var2);
    } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

    return var5;
}
```

上面的 do-while 循环就是一个自旋的操作，var1 是数据对象，var2 是数据偏移（offset），var4 是增量，<u>其中 while 判断里的this.compareAndSwapInt(var1, var2, var5, var5 + var4)，表示成 compareAndSwapInt（obj, offset, expect, update）更加直观</u>。

#### CAS 缺点

##### ABA 问题

可能产生数据轨迹丢失，或者金融问题

##### 自旋时间

自旋时间过长会导致资源线性增长



## 第八章 final 关键字和不变性

#### 不可变对象

对象的所有属性都被 final 修饰，这个对象就是不可变对象

不可变对象一定是线程安全的

#### final 目前的作用

- **防止**类被**继承**、方法被**重写**、变量被**修改**
- 天生**线程安全**，不需要额外同步开销

##### final 修饰变量

> 被 final 修饰的变量，意味着值不能修改。如果变量是对象，也就是变量的引用不能修改，但是对象的内容仍然能被修改

###### final 赋值时机

1. 变量定义的时候就赋值
2. 普通代码块中赋值（如果是 static 修饰的变量，则需要用 static 代码块赋值）
3. 构造函数中赋值（static 修饰则不能）

##### final 修饰方法

1. 构造方法不允许 final 修饰
2. 不可被重写，也就是不能被 override
3. 引申：static 方法不能被重写，但是子类能写一个一样的 static 方法

##### final 修饰类

不希望被继承

#### 不可变对象-栈封闭技术

变量写在方法中

编译时生成的 String 保存在常量池中，运行时产生的在堆中



## 第九章 并发容器

#### 并发容器概览

- **ConcurrentHashMap**：线程安全的 HashMap
- **CopyOnWriteArrayList**：线程安全的 List
- **BlockingQueue**：这是一个接口，表示**阻塞队列**，<u>非常适合用于作为数据共享的通道</u>
- ConcurrentLinkedList：线程安全的**非阻塞队列**

##### 特点总结

三类并发容器：Concurrent* , CopyOnWrite* , Blocking*

> Concurrent* ：大部分通过 **CAS 实现**
>
> CopyOnWrite* ：通过**复制原数据**
>
> Blocking* ：通过 **AQS 实现**

#### 过时的同步容器

##### Vector

方法大多被 Synchronized 修饰

##### Hashtable

同上，方法使用 Synchronized 修饰

##### 使用Collections.synchronizedList

使用这些方法，同样是使用 synchronized，只不过是用来包裹代码块，速度也不快

#### ConcurrentHashMap

##### Map

- HashMap：最常用
- Hashtable：历史遗留，不用
- LinkedHashMap：HashMap 的一个子类，保存了插入的顺序
- TreeMap：**实现了 SortedMap**，能够**实现排序**，默认升序

##### HashMap 多线程下的问题

- 同时 put 碰撞导致数据丢失
- 同时 put 扩容导致数据丢失
- 造成 CPU 100%
  主要原因就是 map 在扩容的时候，可能发生链表循环

##### ConcurrentHashMap putVal 流程

1. 判断 key， value 不为空
2. 计算 hash 值
3. 根据对应位置节点的类型，来赋值，或者增长链表，或者给红黑树增加节点
4. 检查满足阈值就 转为红黑树结构
5. 返回 oldVal

##### ConcurrentHashMap 1.7 到 1.8 的升级内容

- 数据结构
  1.7 使用的 Segment 结构，在16个桶上进行并发操作
  1.8 使用的类 HashMap 的结构，每个 hash 碰撞的地方都能进行并发
- hash 碰撞
  原理同 HashMap
- 保证并发安全
  1.7 采用分段锁 Segment，Segment 继承自 ReentrantLock
  1.8 使用 CAS + synchronized
- 查询复杂度
  1.8 更快了

##### ConcurrentHashMap 的正确使用方法

> *replaceNode(Object key, V value, Object cv)*

```java
Integer score = scores.get("xiao");
Integer newScore = score + 1;
scores.replace("xiao", score, newScore);
```

使用上面的方法进行操作，可以保证数据正确

>  putIfAbsent(K key, V value)

如果没有值，添加；如果有值，获取并返回

##### ConcurrentHashMap 如何保证可见性

根据 happens-before 原则，ConcurrentHashMap 的读写操作会调用 U.getObjectVolatile 或者 U.putObjectVolatile，U 就是 Unsafe 类，这两个方法的描述都是使用了 Volatile，都是能保证可见性的操作

#### CopyOnWriteArrayList

##### 诞生

- 为了代替 Vector 和 SynchronizedList，同 ConcurrentHashMap 代替 HashMap 一样
- Vector 和 SynchronizedList 的**锁粒度太大**，**并发效率低**，并且**迭代时无法编辑**

##### 使用场景

- **读操作尽可能快**，写操作即使慢也没关系
- **读多写少**：黑名单，每日更新，监听器（迭代操作远多于修改操作）

##### 读写规则

读读共享，其他互斥

CopyOnWriteArrayList 的读写规则升级：<u>读取完全不加锁，同时**写入也不会阻塞读取操作**。只有**写入-写入需要同步等待**</u>

##### 设计理念

读操作与一般情况相似，写操作时，先将数组 copy 一个副本，写操作完成后修改原对象的引用，其他读操作完成后，原来的引用将被回收。

<u>对于 add 操作，在源码中使用 ReentrantLock 进行加锁，操作，多个类似修改操作都会被这个 lock 阻塞，而 get 方法完全没有锁的限制。</u>

#### 并发队列

##### Java 并发队列实现关系

- Queue
  - **ConcurrentLinkedQueue**（非阻塞队列）
  - *BlockingQueue*（阻塞队列）
    - **SynchronousQueue**
    - **ArrayBlockingQueue**
    - **PriorityBlockingQueue**
    - **LinkedBlockingQueue**

##### 阻塞队列

阻塞队列本身是线程安全的，一般将一端给消费者用，一端给生产者用

put -->[] [] [] [] --> take 

> 阻塞：
>
> take() : 获取并移除队列头节点，队列**无数据，则阻塞**
>
> put() : 插入元素，队列**已满，则阻塞**

有界，容量大小，特别大的视为无界

阻塞队列是线程池的重要组成部分

#### BlockingQueue

##### 主要方法

- put, take（阻塞方法）
- add, remove, element (非阻塞，**失败将抛出异常**)
- offer, poll, peek (**返回值**可判断操作是否成功，**不抛异常**)

##### ArrayBlockingQueue

put 方法：检查 null，使用 lockInterruptibly()，可打断

#### ConcurrentLinkedQueue

<u>使用 **CAS** 非阻塞算法实现线程安全</u>，并发性能较高

##### 选择合适队列

- 边界（有界、无界）
- 空间
- 吞吐量（ArrayBlockingQueue < LinkedBlockingQueue）

## 第十章 控制并发流程

#### 并发流程工具类



#### CountDownLatch 类

- 倒数门闩
- 流程执行需要某个计数达到一定量，倒数结束之前，**这个线程**处于等待状态。

##### 用法一：一等多

一个线程等待多个线程都执行完，在继续执行自己的工作

多个检察员检查项目，都检查完了，项目再继续

##### 用法二：多等一

运动员跑步

#### Semaphore 信号量

> 限制**有限资源**的使用

多个线程只有有限个能得到执行

- 构造方法，可**设置是否公平**
- acquire()
- tryAcquire
- release()

#### Condition 接口（条件对象）

条件不满足，进入 await 方法，满足后被唤醒

##### 生产者消费者模式

```java
ReentrantLock lock = new ReentrantLock();
Condition nutFull = lock.newCondition();
Condition notEmpty = lock.newCondition();
new Runnable() {
    run() {
        // 判断队列满了
        notFull.await();
        // 等到不为空了，被唤醒  
		notEmpty.signAll();
    }
}

```

await 和 signal 方法和 Object.wait / notify 方法性质相似，但是使用 Lock 而不是 synchronized 有一个优点，就是 lock 对象可以生成多个 Condition，在不同的情况可以更加灵活。

#### CyclicBarrier 循环栅栏

- 和 CountDownLatch 相似，都能阻塞一组线程
- 

##### 与 CountDownLatch  的区别

- 与 CountDownLatch 相比，**CyclicBarrier 可以重用**
- CountDownLatch 是**针对事件**的，使用 countdown() 方法
- CyclicBarrier 是**针对线程**的，每个线程调用 await() 方法

## 第十一章 AQS

AQS：AbstractQueuedSynchronizer，抽象队列化同步器？

#### 为什么需要 AQS

ReentrantLock 和 Semaphore，包括 CountdownLatch、ReentrantReadWriteLock 都有**类似协作的功能**，底层都是用了**共同基类：AQS**

为了实现这些类相同的功能，从而提取出了一个工具，就是 AQS

#### jdk 工具类如何使用 AQS

在工具类中，都有类似 Sync 的内部类，这个内部类继承自 AQS，并且实现了一些方法。

也就是通过内部类的方式，使用 AQS 的功能

#### AQS 的职责

顶层工具类都只是去实现自己需要完成的内容，其他的琐事都使用 AQS 来处理

##### 通过 HR 的例子来比喻

- Semaphore：一个人面试完了，后一个人才能进来面试
- CountdownLatch：群面，等待 10 个人到齐

##### AQS 做了哪些实现

- 同步状态的原子性管理
- 线程的阻塞与解除阻塞
- 队列的管理
- 线程争抢
- ...

##### 总结 AQS 的作用

> AQS 是一个用于构建**锁、同步器、协作工具类**的工具类（框架）

#### AQS 三要素

##### 核心三个部分

- **state**
- 控制线程前所和配合的 **FIFO 队列**
- 期望协作工具类去实现的**获取/释放**等重要**方法**

##### state

```java
private volatile int state;
```

不同工具类中有不同含义， Semaphore 中表示“剩余的许可证数量”，CountdownLatch 中表示“还需要的倒数数量”

由于使用 volatile 修饰，是为了并发安全，所有修改 state 的方法都需要保证线程安全， getState, setState, compareAndSetState 操作都依赖于 J.U.C.atomic 包的支持

##### FIFO 队列

AQS 是一个**排队管理器**，用来存放“等待的线程”

##### 获取/释放方法

#### AQS 分析

##### 在 CountdownLatch 中的使用

- 调用 CountdownLatch 的 await 方法，会尝试获取“共享锁”，不过一开始是获取不到该锁的，于是线程被阻塞
- 共享锁获取的条件是锁计数器的值为0
- 锁计数器的初始值为 count，每当一个线程调用该 CountdownLatch 对象的 countDown() 方法时，才将锁计数器 -1
- count 个线程调用 countdown() 之后，锁计数器为 0，而前面提到的等待获取共享锁的线程才能继续运行

## 获取子线程的结果

#### Future 和 Callable

##### Runnable 的缺陷

- 无法获取返回值
- 无法抛出异常

##### Callable接口

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

声明了泛型返回值，同时抛出了 Exception。

##### Callable 和 Future 的关系

- Future.get 获取 Callable 接口返回的执行结果
- Future.isDone 判断是否执行完成
- call() 未执行完成前，调用 get() 的线程将被阻塞，知道 call() 方法执行完成，返回结果
- Future 类似存储器，存储了 call() 任务的结果

##### get() 方法的五种情况

1. 正常执行完成，返回结果
2. 仍在执行中，get() 线程阻塞
3. 任务执行中**抛出 Exception**，无论真正的异常是什么，get 获取到的异常都是 **ExecutionException**
4. 任务被**取消**，get() 抛出 **CancellationException**
5. 任务**超时**，get() 有个重载方法，传入一个延迟时间，如果时间到了没有获得结果，get() 抛出 **TimeoutException**

##### Future 接口的方法

- cancel(boolean): boolean
- isCancelled(): boolean
- isDone(): boolean
- get(): V
- get(long, TimeUnit): V

#### submit 返回 Future 对象，快速抽取返回值

> 对线程池提交任务，提交时线程池会立刻返回一个空的 Future 容器。当任务执行完毕，可以获取结果的时候，线程池会把该结果填入之前的 Future 中。

```java
ExecutorService service = Executors.newFixedThreadPool(10);
Future<Integer> future = service.submit(new CallableTask());
try {
    System.out.println(future.get());
} catch (InterruptedException e) {
    e.printStackTrace();
} catch (ExecutionException e) {
    e.printStackTrace();
}
service.shutdown();
```

#### 执行时异常

异常在调用 Future 的 call() 方法时，才会捕获到

isDone 方法在 call() 方法结束并退出的时候，就会返回为 true，无论任务执行成功与否、是否报错。

#### Cancel 方法

cancel 方法参数 （是否进行打断），如果 true，将打断 call 方法的执行；如果 false，则方法将继续执行

##### 方法执行中，使用 cancel()

cancel(true) 适用于

- 任务能够处理 interrupt 异常

cancel(false) 适用于

- 未能处理 interrupt 的任务
- 不清楚是否支持取消
- 需要等待已经盖世的任务执行完成（使用 cancel 这些任务将不会执行）

#### FutureTask

RunnableFuture 接口同时继承了 Runnable 和 Future 两个接口，而 FutureTask 实现了 RunnableFuture 接口，也就是同时具有 Runnable 和 Future 的功能。线程可传入 FutureTask 作为参数执行，也能通过 FutureTask 获取到执行结果。





HashMap

final


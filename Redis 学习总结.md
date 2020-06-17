redis 主要内容分析

1. 常见数据类型
2. key 操作，db 操作
3. jedis
4. 持久化（rdb，aof）
5. save
6. 事务
7. 删除策略（过期，定时、定期删除，逐出策略）
8. 高级数据类型（bitmaps，HyperLogLog，GEO）
9. 主从复制（三个阶段）
10. 哨兵模式
11. 集群
12. 解决方案（面试）
    1. 缓存预热
    2. 雪崩
    3. 击穿
    4. 穿透
    5. 性能指标监控

# Redis 基础

## 数据类型

#### List 操作

**lpop** 操作在数组中没有值时会**阻塞**等待

负数表示倒数下标

```shell
insertlinsert key BEFORE|AFTER pivot value
```

**RPOPLPUSH source destination:**

移除 source 的队尾元素添加到 destination 的队首并返回

#### zset 操作

```
zadd key score 1 member1 [score2 member2]
```

添加或更新 member 的分数
member 不可重复，设置相同的 member 将更新对应 score
score 可重复
根据 score 进行排序，**默认升序**

## Redis 命令

#### redis 系统管理

exist：是否存在
randomkey：随机 key
rename：修改 key，新的存在则覆盖
renamenx：同上，但不覆盖
dbsize：总的 key 数量

#### 时间相关

expire：设置 key 过期时间，**时间未到再次设置将刷新**
ttl：查询 key 剩余时间
flushdb：清空当前数据库的所有 key
flushall：清空所有数据库的所有 key

key 过期

```
expire key
```


key 取消过期

```
persist key
```

两种设置密码的方式：

1. config set requirepass xxx
2. 修改 /etc/redis/redis.conf 文件，通过 grep 命令查找关键字在目标文件中的行数，根据这个行数，插入以下
   requirepass xxx（密码）

设置完成后，两种登录方式

1. redis-cli -a xxx（密码）
2. redis-cli （登录）

>auth xxx

## 使用 jedis 操作 redis

jedis 依赖
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>

#### application.yml 配置

```yml
server:
  port: 8080
spring:
  redis:
    port: 6379
    host: 192.168.48.126
    jedis:
      pool:
        max-active: 10
        max-idle: 6
        min-idle: 2
    timeout: 2000
#    password:
```

#### 重写 redisConnectionFactory 的==序列化方式（可面试）==

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
	RedisTemplate<String, Object> template = new RedisTemplate<>();
	template.setConnectionFactory(factory);
    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
    ObjectMapper om = new ObjectMapper();
    om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(om);

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    // 在使用注解 @Bean 返回 RedisTemplate 的时候，同时配置 hashKey 与 hashValue 的序列化方式
    // key 采用 String 的序列化方式
    template.setKeySerializer(stringRedisSerializer);
    // value 序列化方式采用 jackson
    template.setValueSerializer(jackson2JsonRedisSerializer);

    // hash 的 key 也采用 String 的序列化方式
    template.setHashKeySerializer(stringRedisSerializer);
    // hash 的 value 序列化方式采用 jackson
    template.setHashValueSerializer(jackson2JsonRedisSerializer);
    template.afterPropertiesSet();
    return template;
}
```

使用默认的序列化方式会导致在 redis 中缓存的数据前面加了一些字符，类似这样

![Image](.\image\Redis 学习总结\redis序列化.png)

使用上面的序列化方式则不会产生这些字符

## 服务器基础配置

#### 服务端设定

```
# 设置是否以守护进程运行
daemonize yes|no

# 绑定主机地址
bind  127.0.0.1

# 服务器端口
port 6379

# 数据库数量
databases 16
```

#### 日志配置

```
# 日志级别
loglevel debug|verbose|notice|warning

# 日志记录文件名
logfile  端口号.log
```

#### 客户端配置

```
# 同一时间最大客户端连接数，0表示无限制
maxclients 0

# 客户端闲置等待最大时长
timeout 300
```

#### 多服务器快捷配置（类似继承）

```
# 导入并加载指定配置文件
include /path/server-端口号.conf
```



# Redis 高级

## 持久化机制

#### 两种持久化方式：

1. snapshotting（**快照 rdb**）：默认方式，将数据存放到文件中。默认文件 dump.rdb，可配置 Redis 在 n 秒内如果超过 m 个 key 被修改，就自动保存。save 300 10：300 秒内超过 10 个key被修改，就快照保存
2. Append-only file（缩写 **AOF**）：读写操作存放到文件

快照方式由于有时间间隔，如果在此之前 down 机，则最后的几个操作将丢失aof 比快照有更好的持久性，每个写命令都通过 write 函数写到文件中

```
appendonly yes //启用 aof 持久化方式

# appendfsync always //收到写命令就立即写入磁盘，最慢，但是保证了数据的完整持久化

appendfsync everysec //每秒钟写入磁盘一次，在性能和持久化方面做了很好的折中

# appendfsync no //完全依赖 os，性能最好，持久化没有保证
```

#### aof 重写

重写方式：

- 手动重写
  **bgrewriteaof**
- 自动重写
  **auto-aof-rewrite-min-size**   size
  **auto-aof-rewrite-percentage**  percentage

## redis 事务

使用 **multi 开启事务**
使用 **exec 执行事务**

使用 discard 放弃队列执行

#### redis 事务错误处理

1. 命令执行错误，该条命令不执行，其他正常执行，不回滚
2. 命令本身语法错误，直接报错，事务取消，回滚

#### 事务 锁-watch

```
watch key
multi
incr a
get a    # 此时另一个客户端修改了 a 
exec
> nil    # 此次事务执行失败
```

使用 watch 监视一个 key，在监事过程中，开启了事务，如果被监视的值在其他进程中**被修改**，则当前事务执行将**回滚，返回 nil**

#### 事务 分布式锁

使用 setnx 设置一个公共锁，一般配合 expire 使用 

```
setnx lock-key value [expire seconds]
# ...
del lock-key
```

利用 setnx 命令的返回值特征，有值则返回设置失败，无值则返回设置成功
**有值**则进行下一步操作
**无值**，排队或等待

操作完毕**通过 del 释放锁**

#### 事务工作流程

![Image](.\image\Redis 学习总结\事务工作流程.png)

## 删除策略

---

#### 数据过期

![image-20200617103841770](.\image\Redis 学习总结\expires维护过期地址.png)

**删除策略**

定时删除
惰性删除
定期删除（默认，折中）

#### 定时删除-保护内存

创建定时器，当 key 设置有过期时间，切过期时间到达时，由定时器任务**立即**执行对键的**删除**策略

- **优点**：节约内存，到时间就删除，快速释放内存空间
- **缺点**：CPU 压力大，无论 CPU 是否空闲，都会占用 CPU，<u>影响服务响应时间和指令吞吐量</u>

#### 惰性删除-保护 CPU

数据到达过期时间，不作处理。下次访问数据时
    未过期：返回数据
    发现已过期，删除，返回不存在

- 优点：节约 CPU 性能，必须删除时才执行
- 缺点：占用内存很多，过期数据不删除导致内存泄漏

#### 定期删除-折中

![image-20200617105410699](.\image\Redis 学习总结\定期删除.png)

周期性轮询 redis 中的时效性数据，采用随机抽取的策略，利用过期数据占比的方式控制删除频度

- CPU 性能占用设置有峰值，检测频度可自定义设置
- 内存压力不是很大，长期占用内存的冷数据会被持续清理

总结：随机抽查，重点抽查

#### 逐出算法

逐出：内存满了，清理数据，不一定是过期的，**可能抛出 oom 异常**

删除：删除过期数据

##### 影响数据逐出的相关配置

最大可用内存：`maxmemory`，一般设置50%以上

每次选取待删除数据个数：`maxmemory-samples`，不会全库扫描，因此用**随机获取数据**的方式检测

策略：`maxmemory-policy`

##### 检测易失数据:

1. volatile-lru：最近最少使用（Recently）
2. volatile-lfu：最近使用次数最少（Frequently）
3. volatile-ttl：挑选将要过期的数据
4. valatile-random：任意挑选数据淘汰

##### 检测全库数据：

1. allkeys-lru
2. allkeys-lfu
3. allkeys-random

##### 不逐出

## 高级数据类型

---

#### bitmap

使用场景：用于<u>存储状态，统计状态</u>，可以使用交、并、非、异或等操作对数据进行处理存储方式为 10010110 电影点播过

#### HyperLogLog

使用场景：<u>统计数据基数（不重复的数据数量）</u>
具有误差
耗空间极小，每个 key 占用 12K 的内存

#### GEO

计算地理位置（坐标计算）

操作：

```
geoadd geos 1 1 a

geoadd geos 2 2 b

geopos geos a
1) xxx
2) xxx

geodist geos a b [unit（单位）]
```

# 集群

## 主从复制

---

### 主从复制作用

- 读写分离
- 负载均衡
- 故障恢复：slave 转 master
- 数据冗余：数据热备份，区别于持久化
- 高可用（HA）

### 主从复制工作流程

**总述：**

三个阶段：

1. 建立连接
2. 数据同步
3. 命令传播

#### 建立连接阶段（准备）

![image-20200617130255839](.\image\Redis 学习总结\主从建立连接.png)

1. 设置 master 的地址
2. 建立 socket 连接
3. 发送 ping 命令
4. 身份验证
5. 发送 slave 端口信息

连接成功，状态如下：

slave：
    保存 master 的地址与端口

master：
    保存 slave 的端口

总体：
    建立了socket 连接

#### 主从连接

方式一：客户端发送命令

```
slaveof <masterip> <masterport>
```

方式二：启动服务参数

```
redis-server -slaveof <masterip> <masterport>
```

方式三：服务器配置

```
slaveof <masterip> <masterport>
```



#### 数据同步阶段工作流程

1. 请求同步数据
2. 创建 RDB 同步数据
3. 恢复 RDB 同步数据
4. 请求部分同步数据
5. 恢复部分同步数据

![Image](.\image\Redis 学习总结\rdb数据同步.png)

#### ==复制缓存区设置大小合理==

```
repl-backlog-size
```

#### 设置大小推荐：

1. master 到 slave 重连平均时长 second
2. master 平均每秒写命令数据总量 write-size-per-second
3. 最优空间 = 2 * second * write-size-per-second

#### 命令传播阶段

服务器运行 ID（runid）进行身份认证

#### 复制缓冲区工作原理

- 组成
  - 偏移量
  - 字节值
- 工作原理
  1. 通过 offset 区分不同的 slave 当前数据传播的差异
  2. master 记录已发送的信息对应的 offset
  3. slave 记录已接收的信息对应的 offset
  4. **各记各的**

##### 先进先出的队列

保存 master 收到的所有指令

##### 偏移量（offset）

用于比对 master 和 slave 的差别

![Image](.\image\Redis 学习总结\主从复制流程.png)

![Image](.\image\Redis 学习总结\主从复制（完整）.png)

psync2
replconf ack offset

### 常见问题

##### 频繁全量复制

问题原因及解决：

1. master 重启，导致 runid 改变，slave 必须进行全量复制
   内部的解决方案。。
2. 网络问题导致重连，期间的数据缓冲超过了缓冲区空间溢出，导致全量复制
   解决见上面**复制缓冲区**部分

##### 频繁网络中断

现象：
    master 的 CPU 占用过高

原因：慢查询

解决：设置合理的超时时间
    ping 的频率增加

##### 数据不同步

现象：
    多个 slave 获取相同数据不同步

原因：
    网络不同步，数据发送延迟

解决：

1. 优化主从间的网络环境
2. 通过 offset 监控从节点延迟

##### 现象和解决：

- slave 宕机，影响不大
- master 宕机，从节点转 master
- master 压力大，slave 转 master 并添加相应 slave，变成拓扑结构

##### 连接方式：

1. 启动两个 redis 服务器，在客户端中 slaveof 127.0.0.1 6379
2. 在启动服务时， --slaveof 127.0.0.1 6379
3. 配置文件中，slaveof 127.0.0.1 6379

#### 主从总结

- 什么是主从复制
- 主从复制工作流程
  - 三个阶段
  - 三个核心
    - 运行 id（runid）
    - 复制积压缓存区
    - 复制偏移量（offset）
  - 心跳机制
    - 维护数据传输
- 常见问题

## 哨兵模式

---

#### 起源

​    master 宕机，需要重新选择一台机器当做 master

#### 涉及到的过程

1. 下线宕机的 master
2. 找新的 slave 作为 master
3. 通知所有的 slave 连接新的 master
4. 启动新的 master 与 slave
5. <u>全量复制</u> 和 <u>部分复制</u>

#### 出现的问题

1. 确认 master 宕机
2. 如何选择 slave 作为新的 master
3. 修改配置后，原来的 master 恢复了

#### 哨兵

哨兵（sentinel）是一个分布式系统，对主从结构的每台服务器进行**监控**，出现故障时进行投票**选择新的 master** 并将所有的 slave 连接到新的 master 上

==哨兵也是一个 redis 服务器==

#### 哨兵作用

- 监控
  - 检查 master 和 slave
- 通知
  - 向其他（哨兵，客户端）发送通知
- 自动故障转移
  1. 断开 master 与 slave 连接
  2. 选取一个 slave作为 master
  3. 将 slave 连接到新的 master
  4. 通知客户端新的服务器地址

#### 配置文件

1. **sentinel monitor <master-name> <ip> <port> <quorum>**
   - master-name：master名称（可以自定义）
   - ip port : IP地址和端口号
   - quorum：票数，Sentinel需要协商同意master是否可到达的数量。
2. **sentinel down-after-milliseconds <master-name> <times>** 
   - 哨兵定期发送 ping 指令来判断 redis 是否可达
3. **sentinel parallel-syncs <master-name> <nums>** 
   - master 挂了，选择新的 master，slave 向新的 master 发起同步数据，这个设置表示<u>允许并行同步的 slave 个数</u>
4. **sentinel failover-timeout <master-name> <times>**  
   - 超过 times，则故障转移失败
5. **sentinel auth-pass <master-name> <password>**  
   - redis 主节点设置了密码，需要进行这个配置

```
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel down-after-milliseconds mymaster 30000
sentinel parallel-syncs mymaster 1
sentinel failover-timeout mymaster 180000
```

### 哨兵工作原理

#### 阶段一：监控阶段

![Image](.\image\Redis 学习总结\哨兵阶段一：监控阶段.png)

![Image](.\image\Redis 学习总结\哨兵阶段一：监控阶段2.png)

#### 阶段二：通知阶段

![Image](.\image\Redis 学习总结\哨兵阶段二：通知阶段.png)

#### 阶段三：故障转移阶段

![Image](.\image\Redis 学习总结\哨兵阶段三：故障转移阶段.png)

#### 总结

- 监控
  - 同步信息
- 通知
  - 保持联通
- 故障转移
  - 发现问题
  - 竞选负责人
  - 优选新 master
  - 新 master 上任，其他 slave 切换 master，原 master 作为 slave 故障恢复后连接

## 集群

---

集群解决了：

- 单机**宕机的问题**
- 单机**访问压力问题**

#### 数据存储设计

![Image](.\image\Redis 学习总结\Redis数据存储设计.png)

- 通过算法得到**数据应该存储的位置**
- 将所有的存储空间分割成若干份（16384份），每台主机保存一部分
  每份代表一个存储空间（不是 key）
- 将 key 按照计算出的结果放到对应存储空间

**引出的问题：**
		增加或者有服务器宕机，怎么办

#### 集群内部通讯设计

![Image](E:\mdnote\image\集群内部通讯设计.png)

- 各个数据库相互通信，保存各个库中槽的编号数据
- 一次命中，直接返回
- 一次未命中，返回在其他库中的具体位置
- ==最多两次即可命中==

## 企业级解决方案

---

#### 缓存雪崩

问题：瞬间过期数据量太大，对数据库造成压力
路线：避免过期时间集中

**解决（道）**

1. 更多的页面静态化处理
2. 构建多级缓存架构
     Nginx 缓存 + redis 缓存 + ehcache缓存
3. 检测 MySql 严重耗时业务进行优化
     对数据库的瓶颈排查：如超市查询，高耗时事务
4. 灾难预警机制，监控 redis 服务器性能指标
   - CPU 占用、CPU 使用率
   - 内存容量
   - 查询平均响应时间
   - 线程数
5. 限流、降级
     短时间牺牲一些客户体验，限制一部分请求访问，降低应用

**解决（术）**

1. 与 LFU 切换
2. 数据有效期策略调整
   - 根据业务数据有效期进行**分类错峰**，A 类 90 分钟，B 类 80 分钟，。。。
   - 过期时间使用**固定时间 + 随机值**的形式，稀释集中到期的 key 的数量
3. **超热数据**使用**永久 key**
4. 定期维护（自动 + 人工）
   对即将过期数据做访问量分析，确认是否延时，配合访问量统计，做热点数据的延时
5. 加锁

#### 缓存击穿

单个高热数据过期，同时多个数据访问，redis 未命中，导致大量访问进入数据库

**解决（术）预防**

1. 根据业务，对某些 key 增加过期时间
2. 现场调整
     监控访问量，对**流量激增的数据延长过期时间**或设为永久 key
3. 后台刷新数据
     启动定时任务，**高峰期之前刷新**数据有效期，确保不丢失
4. 二级缓存
     设置不同的失效时间，保障**不被同时淘汰**
5. 加锁
     分布式锁，防止击穿，**慎用**

#### 缓存穿透

**问题：**

1. Redis 中大量未命中
2. 出现非正常 URL 访问

**解决（术）：**

1. 缓存 null
     对查询结果为 null 的数据进行缓存（长期使用，定期清理），设定短时限，30秒-60秒
2. 白名单策略
   - 提前预热分类数据 id 对应的 bitmaps，id 作为 bitmaps 的 offset，相当于设置了数据白名单，加载正常数据则放行，异常数据拦截
   - 使用布隆过滤器
3. 实时监控
     实时监控 redis 命中率与 null 数据的占比
   - 非活动时：3-5倍，超过则纳入重点排查
   - 活动时：10-50倍，超过则纳入重点排查
4. key 加密
   临时启动防灾业务 key，对 key 进行业务层传输加密服务，设定校验程序   
   例如每天分配60个加密串，挑2到3个，混到页面数据 id 中，发现访问 key 不满足规则，驳回访问

## 杂项

---

##### 设置端口开放

```
firewall-cmd --zone=public --add-port=6379/tcp --permanent
```

##### 设置外部访问

注释掉 redis.conf 中的 bind 127.0.0.1

##### linux 查找 redis 进程

```
ps -ef | grep -i redis
```

##### sudo 启动 redis-server

```
sudo /usr/local/redis/bin/redis-server
```

##### 使用 docker 启动redis

```
# docker 启动 redis 服务
docker run -d --name redis-6379 -p 6379:6379 redis --requirepass "jettx"

# docker 执行 redis 客户端
docker exec -it redis-6379 redis-cli -a jettx
```




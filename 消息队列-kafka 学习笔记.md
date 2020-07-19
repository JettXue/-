## 概略

### 数据结构概览

- Topic
  - partition   -- log  -- index（记录每个 segment 中第一条消息偏移）
    - segment 文件
      - message
      - message
      - ...
    - segment
    - segment
    - ...
  - partition -- log
  - partition -- log
- Topic
- Topic
- ...

发布者发到某个 Topic 的消息会被均匀的**分布到多个 Partition** 上（随机或根据用户指定的回调函数进行分布），Broker 收到发布消息往**对应 Partition 的最后一个 segment 上添加该消息**。
当某个 segment上 的消息条数达到配置值或消息发布时间超过阈值时，segment上 的消息会被 flush 到磁盘，只有 flush 到磁盘上的消息订阅者才能订阅到，segment 达到一定的大小后将不会再往该 segment 写数据，Broker 会**创建新的 segment** 文件。

### 整理认识

#### 特性

1. **高吞吐量、低延迟**：kafka 每秒可处理几十万消息，每个主题可分多个分区，消费组对分区进行消费操作
2. **可扩展性**：kafka 集群支持<u>热扩展</u>
3. **持久性、可靠性**：消息被<u>**持久化**到本地磁盘，并且支持**数据备份**</u>
4. **容错性**：<u>**允许**集群中节点**失败**</u>（若副本数量为 n，则允许 n-1 个节点失败）
5. **高并发**：支持数千个客户端<u>**同时读写**</u>



#### 使用场景

1. **日志收集**：可以收集各种服务的 log，通过 kafka 以统一接口服务的方式开放给各种 consumer，例如 Hadoop，Hbase，Solr 等
2. **消息系统**：<u>解耦生产者和消费者</u>，<u>消息缓存</u>等
3. 用户活动跟踪：记录 web 用户或者 app 用户的浏览网页、搜索、点击等活动，这些消息发送到 topic 中，订阅者通过订阅 topic 来做实时监控分析。

#### 技术优势

- 可伸缩性

  - 集群热扩展，添加或删除代理
  - 扩展 topic 来包含更多的分区

- 容错性和可靠性

  kafka 的设计使得某个代理的故障能被集群中的其他代理检测到，又去每个主题都可在多个代理上复制，所以集群可以在不中断服务的情况下从此类故障中恢复并运行

- 吞吐量







## Kafka 概念

#### 结构概念

![img](.\image\消息队列-kafka 学习笔记\kafka架构.png)

如上图，kafka 的结构主要是：

- 集群中有 n 个 Broker
- 有多个 topic
- 每个 topic 中有多个分区（Partition）
- 每个 Broker 中存储 topic 的不同 Partition
- 有 n 个 Broker，某个 topic 有小于 n 个的 Partition，就会存在不同的 Broker 中，<u>**每个 Broker 最多存储 1 个 Partition**</u>
- 如果 Partition 的数量大于 Broker 的数量，就会造成**<u>某个 Broker 中的 Partition 数量大于 1</u>**，导致 kafka 集群的**数据存储不均衡**

#### 消息队列的两种模式

##### 点对点（一对一）

消费者主动拉取数据，消息收到后清除

##### 发布订阅模式（一对多）

消费者消费数据后不会清除消息

有两种方式：

- Topic 主动推送
- 消费者主动拉取





## 竹根项目的 kafka 使用整理

- app 主推消息发送
- 错误日志处理
- 生成字库的后台任务消息，其他服务订阅然后处理












































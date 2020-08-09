# kafka 入门

在 ubuntu 环境下，所以有些命令不太一样

## 安装 kafka 集群

#### 下载 kafka

```shell
cd /opt
sudo wget https://labfile.oss.aliyuncs.com/courses/859/kafka_2.11-1.1.1.tgz

sudo tar -zxvf kafka_2.11-1.1.1.tgz
sudo mv kafka_2.11-1.1.1.tgz kafka

cd /opt/kafka/config
#复制 server.properties 文件为 server-1.properties
sudo cp server.properties server-1.properties
#复制 server.properties 文件为 server-2.properties
sudo cp server.properties server-2.properties
# 当前 config 目录下需要注意的配置文件：
# server.properties server-1.properties server-2.properties
# zookeeper.properties
```

#### 启动 zookeeper

修改上面的配置文件，组成集群

```shell
# 给当前用户添加目录权限
sudo chmod 777 -R /opt/kafka
# 启动 zookeeper
/opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties
# 后台启动 zookeeper
nohup /opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties &

# 查看 java 进程
jps
# QuorumPeerMain 进程说明 zookeeper 启动成功
```

修改三个 **server*.properties** 配置文件

| 配置文件名称        | 修改内容                                                     |
| ------------------- | ------------------------------------------------------------ |
| server.properties   | 此文件不修改，保持默认值                                     |
| server-1.properties | 修改 `broker.id=1`，`log.dir=/tmp/kafka-logs-1`，新增 `listeners=PLAINTEXT://:9093` 三个参数值 |
| server-2.properties | 修改 `broker.id=2`，`log.dir=/tmp/kafka-logs-2` 和 `listeners=PLAINTEXT://:9094` 三个参数值 |

上面三个 Kafka 配置文件，<u>每个配置文件对应 Kafka 集群中一个节点（称为 **Broker**）</u>。

```shell
#使用配置文件 server.properties 启动第一个 Kafka Broker，注意：命令最后的 & 符号表示以后台进程启动，启动完成后，按回车键，回到命令行，启动另一个 Kafka Broker。
./kafka-server-start.sh  ../config/server.properties &
```

依次启动三个 kafka 节点，然后查看 jps

#### kafka 集群中操作 Topic

```shell
# 创建 topic
./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic myFirstTopic

# 查看已有的 topic
./kafka-topics.sh --zookeeper localhost:2181 --list

# 启动 producer
./kafka-console-producer.sh --broker-list localhost:9092 --topic myFirstTopic
# 另起终端，启动 consumer
./kafka-console-consumer.sh --bootstrap-server  localhost:9092 --topic myFirstTopic
```

在消息生产者所在命令行终端中输入 `hello kafka` ，然后按回车键，消息发送到 Topic 。此时在消息消费者所在的命令行中，可以看到 `hello kafka` 消息已经收到了

## kafka 集群概念

#### broker

一台服务器上只会安装一个 Kafka 软件，这台服务器就是一个 Kafka Server，此时 Kafka Server 称为 broker。在我们的实验环境中，一个一台服务器上安装了三个 broker。

#### topic 和 partition

发布到 Kafka 集群上的消息都属于某一个分类，这个分类称为 topic。topic 存储在 broker 中。一个 topic 包括多个 partition（分区），partition 是物理上的概念。producer 发送的消息存储在 partition 中，每个 partition 可以有多个 replica（副本）。

#### producer 和 consumer

producer 是用来产生消息的，负责将消息发送到 Kafka broker 的 topic 上，consumer 是用来消费消息的，用于从 Kafka broker 上的 topic 中读取消息。

broker 和 consumer 使用 Zookeeper 管理状态信息，如 offset 信息。

![img](.\kafka组件.png)

- 上图中展示了 Kafka 相关的 5 个组件：producer、consumer、broker、topic、Zookeeper。
- 上图中展示了一个 broker，broker 中有一个名称为 KafkaTopic 的 Topic，KafkaTopic 中有 3 个 partition ，分别为 partition1 、partition2 、partition3 。
- 消息生产者 producer 将消息发送到 topic 的 partition 中。
- 消息消费者 consumer 从 topic 的 partition 中读取消息。
- consumer 和 broker 的元数据信息保存在 Zookeeper 中。
- 在 topic 中，每一个 partition 对应一个日志文件，partition 是一个有序的、不可变的消息序列。一个消息只能发送到一个 partition 上。

创建 topic 的命令

```shell
./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic myFirstTopic
```

上述命令详细解释如下

- `kafka-topics.sh`：Kafka 提供的一个 shell 脚本文件（位于 bin 目录中），用于创建或查看 topic 信息。
- `--create`：shell 脚本的参数，告诉 shell 脚本要创建一个 topic。
- `--zookeeper localhost:2181`：shell 脚本的参数，告诉 shell 脚本 Zookeeper 的地址用于保存 topic 元数据信息。
- `--partitions 1`：shell 脚本参数，告诉 shell 脚本所创建的这个 topic 的 partition 个数为 1。
- `--replication-factor 1`：shell 脚本参数，告诉 shell 脚本每个 partition 的副本数为 1。
- `--topic myFirstTopic`：shell 脚本参数，告诉 shell 脚本创建的 topic 的名称为 `myFirstTopic`。

#### 创建 topic 并指定 partition 数量和副本数量

创建一个包含 3 个 partition、每个 partition 有 2 个副本的 topic：`mySecondTopic`。

```shell
./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 2 --partitions 3 --topic mySecondTopic
```

查看 `mySecondTopic` 的 partition（分区）数和分区副本数

```shell
#运行查看topic信息的shell脚本
./kafka-topics.sh --zookeeper localhost:2181 --describe --topic mySecondTopic

# 运行结果
Topic:mySecondTopic	PartitionCount:3	ReplicationFactor:2	Configs:
	Topic: mySecondTopic	Partition: 0	Leader: 2	Replicas: 2,0	Isr: 2,0
	Topic: mySecondTopic	Partition: 1	Leader: 0	Replicas: 0,1	Isr: 0,1
	Topic: mySecondTopic	Partition: 2	Leader: 1	Replicas: 1,2	Isr: 1,2

```

第一行 topic 信息概述：Topic 名称为 `mySecondTopic`，partition 数量为 3，副本数为 2，三个 partition 的详细配置信息见下面三行。紧接着三行信息，每一行描述了一个 partition 的信息，后面的 <u>leader、replicas 和 ISR 涉及到 kafka 内核结构</u>，我们后续单独开辟实验详细讲解，此时你只需要知道，这些参数保证了 kafka 集群的健壮性，保证消息不丢失，保证消息的高吞吐量。

#### 查看 zookeeper 上的元数据信息

```shell
# 运行 zookeeper-shell.sh 进入 zookeeper 的 shell 模式
./zookeeper-shell.sh localhost:2181

# 输入 ls /
Connecting to localhost:2181
Welcome to ZooKeeper!
JLine support is disabled

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
ls /
[cluster, controller, controller_epoch, brokers, zookeeper, admin, isr_change_notification, consumers, log_dir_event_notification, latest_producer_id_block, config]
```

在 zookeeper shell 模式下，可输入 `--help` 查看所有命令

```shell
# 查看 brokers 节点的子节点
ls /brokers 
> [ids, topics, seqid]
# 列出集群中的 broker id。因为我们的实验环境里是三个 broker 的集群，并且我们配置了三个配置文件(server.properties、server-1.properties、server-2.properties)，所以结果列出了[0,1,2]
ls /brokers/ids
#列出 broker 中的 topic，可以看到我们创建的两个 topic，第三个 topic 是 kafka 内置 topic，用于存储 offset 相关信息。
ls /brokers/topics
[mySecondTopic, myFirstTopic, myFirstTopic2, __consumer_offsets]

get /brokers/topics/mySecondTopic
{"version":1,"partitions":{"2":[1,2],"1":[0,1],"0":[2,0]}}
cZxid = 0xbd
ctime = Thu Aug 06 23:27:14 CST 2020
mZxid = 0xbd
mtime = Thu Aug 06 23:27:14 CST 2020
pZxid = 0xbf
cversion = 1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 58
numChildren = 1
```

> 注意：如果 `ls` 命令返回`[]`,说明这个节点是叶子节点，可以运行 `get` 命令获取内容

Kafka 使用 Zookeeper 保存当前 Kafka 集群中的集群成员（即 broker）。一个 broker 进程启动时，broker 进程使用它的 `broker.id` 值在 Zookeeper 中注册了自己。

## kafka topic

#### topic 参数说明

- **topic**：用于划分 Kafka 集群中的**消息（Message）的逻辑概念**，生产者发送消息，要发送给某个 topic；消费者消费消息，要消费某个（某几个）topic 中的消息。Kafka 集群中的消息都存储在 topic 中。
- **Partitions（分区）**：<u>topic 中的消息是以 partition 分区的形式存储的</u>，注意：partition 是 Kafka 支持扩展和高并发处理的基础。<u>每个 topic 包括 1 个或多个 partition</u>。
- **Offset**：每个 partition 中的<u>消息都是**顺序存储**的</u>，每个消息都是有编号的，编号是顺序增长的。这个编号叫 offset，offset 记录了消息在 partition 中的位置。注意：offset 在一个 partition 内是有序的，<u>多个 partition 之间的 offset 是独立存在的</u>。
- **Replication（副本）**：Kafka 支持以 partition 为单位对消息进行冗余备份，每个 Partition 都必须配置至少 1 个 replication。replication-factor 参数指定了 replication 的个数，即 partition 的副本个数。**每个 topic 的副本个数不能设置超过 broker 的个数**。
- Leader replica：在 partition 的多个副本 replication 中，有一个副本叫主副本（即 Leader replica），所有消息消费者和消息生产者的读写请求都由 Leader replica 处理，这么做是为了保证一致性。其他副本叫从副本（即 Follower replica），从副本从主副本处把数据更新同步到本地。
- **ISR**（全拼为：In-Sync Replica）：从副本中，如果从主副本处把数据更新同步到本地了，那么这个从副本处于 ISR 状态（已同步状态），如果没有完全与主副本同步，那么会被从 ISR 中踢出去，处于非同步状态。

修改分区个数

```shell
#注意：shell命令中的关键参数--alter,将partition数量由原来的3改为4
./kafka-topics.sh --zookeeper localhost:2181 --alter --topic mySecondTopic --partitions 4
```

如果 topic 的 partition 数量增加了，那么消息的分区逻辑或者消息的顺序都会受到影响，**partition 的个数只能增加**

删除 topic

```shell
#注意：shell命令中的关键参数--delete。
./kafka-topics.sh --zookeeper localhost:2181 --delete --topic myDeleteTopic
```

以上操作只是标记删除，没有真正删除。如果需要完全删除 topic，需要配置文件 `server.properties` 中修改 `delete.topic.enable=true`

#### 使用 java 语言调用 kafka API

```java
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import scala.collection.Iterable;
import scala.collection.Map;

import java.util.Properties;

public class TrainKafkaTopic {
    public static void main(String[] args) {
        //使用工具类连接zookeeper localhost:2181 session失效时间和连接超时时间都是30s,不启用kafka安全模式(false)
        ZkUtils zu = ZkUtils.apply("localhost:2181", 30000, 30000, false);
        //获取所有的topic配置信息，返回结果map中的key是topic名称
        Map<String, Properties> map = AdminUtils.fetchAllTopicConfigs(zu);
        //获取所有的topic名称
        Iterable<String> allTopics = map.keys();
        //将topic名称用&拼接起来
        String topicResult = allTopics.mkString("&");
        System.out.println(topicResult);
    }
}
```

#### kafka 消息存储位置

在 Kafka broker 配置文件 `server.properties` 中，我们配置了参数：`log.dirs=/tmp/kafka-logs`，这个参数值<u>指定了 topic 中消息存储的**物理位置**</u>。topic 中的 partition 是以文件的形式存储在文件系统中，比如，创建了一个名为 `mySecondTopic` 的 topic，其有 4 个 partition，那么在 Kafka 的数据目录中（由配置文件中的 `log.dirs` 指定的）中就有这样 <u>4 个目录: `mySecondTopic-0`， `mySecondTopic-1`，`mySecondTopic-2`，`mySecondTopic-3`</u>，其命名规则为：

```xml
<topic_name>-<partition_id>
```

查看 topic 详情

```shell
shiyanlou:bin/ $ ./kafka-topics.sh --topic t1 --zookeeper localhost:2181 --describe 
Topic:t1	PartitionCount:4	ReplicationFactor:3	Configs:
	Topic: t1	Partition: 0	Leader: 2	Replicas: 2,1,0	Isr: 2,1,0
	Topic: t1	Partition: 1	Leader: 0	Replicas: 0,2,1	Isr: 0,2,1
	Topic: t1	Partition: 2	Leader: 1	Replicas: 1,0,2	Isr: 1,0,2
	Topic: t1	Partition: 3	Leader: 2	Replicas: 2,0,1	Isr: 2,0,1

```

详细结果中的第一行说明：`mySecondTopic-0` 的 Replicas 为 2,1,0，意思是 `mySecondTopic-0` 目录位于 broker id 为 2 和 1 和 0 的 `log.dirs` 指定的目录中。

详细结果中的第一行说明：`mySecondTopic-0` 的 Replicas 为 1,2，意思是 `mySecondTopic-0` 目录位于 broker id 为 1 和 2 的 `log.dirs` 指定的目录中。以此类推。

## Producer 开发

通过 Producer 类进行的基本操作

```java
//创建一个Properties对象，用于存储连接kafka所需要的配置信息
Properties kafkaProps = new Properties();
//配置kafka集群地址
kafkaProps.put("bootstrap.servers", "localhost:9092");
//向kafka集群发送消息,除了消息值本身,还包括key信息,key信息用于消息在partition之间均匀分布。
//发送消息的key,类型为String,使用String类型的序列化器
kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//发送消息的value,类型为String,使用String类型的序列化器
kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//创建一个KafkaProducer对象，传入上面创建的Properties对象
KafkaProducer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);
/**
 * 使用ProducerRecord<String, String>(String topic, String key, String value)构造函数创建消息对象
 * 构造函数接受三个参数：
 * topic--告诉kafkaProducer消息发送到哪个topic;
 * key--告诉kafkaProducer，所发送消息的key值，注意：key值类型需与前面设置的key.serializer值匹配
 * value--告诉kafkaProducer，所发送消息的value值，即消息内容。注意：value值类型需与前面设置的value.serializer值匹配
 */
ProducerRecord<String, String> record =new ProducerRecord<>("mySecondTopic", "messageKey", "hello kafka");
try {
  //发送前面创建的消息对象ProducerRecord到kafka集群
  //发送消息过程中可能发送错误，如无法连接kafka集群，所以在这里使用捕获异常代码
  producer.send(record);
  //关闭kafkaProducer对象
  producer.close();
} catch (Exception e) {
    e.printStackTrace();
}
```

通过设置 `Properties` 对象的不同参数，可以完成对 producer 对象的控制。
上述代码片段实例化了一个 producer，接着调用 `send()` 方法发送消息。

#### send() 方法

- **Fire-and-forget**（发送即忘记）：此方法用来发送消息到 broker，<u>不关注消息是否成功到达</u>。大部分情况下，消息会成功到达 broker，因为 Kafka 是高可用的，并且 producer 会<u>自动重试发送</u>。但是，还是会有消息丢失的情况；本实验用的就是这种方法。
- **Synchronous Send**（同步发送）：发送一个消息，send() 方法返回一个 Future 对象，使用此对象的 get() 阻塞方法，可以根据 send() 方法是否执行成功来做出不同的业务处理。此方法关注消息是否成功到达，但是由于使用了同步发送，消息的发送速度会很低，即吞吐量降低。
- **Asynchronous Send**（异步发送）：以回调函数的形式调用 send() 方法，当收到 broker 的响应，会触发回调函数执行。此方法既关注消息是否成功到达，又提高了消息的发送速度。




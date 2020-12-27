首先linux安装后的配置

## Linux网络配置

**修改 `/etc/resolv.conf`**
`nameserver 114.114.114.114`

**修改 `/etc/sysconfig/network-scripts/ifcfg-ens33`**

```properties
TYPE="Ethernet"
BOOTPROTO="static"
NAME="ens33"
UUID="5604863a-4f2d-4725-8be7-ef9ac3823295"
DEVICE="ens33"
ONBOOT="yes"
IPADDR="192.168.48.22"
PREFIX="24"
GATEWAY="192.168.48.2"
```

**修改 `/etc/sysconfig/network`**

```properties
NETWORKING=yes
NETWORKING_IPV6=no
HOSTNAME=node2
GATEWAY=192.168.48.2
```

**修改 `/etc/hosts`**

`192.168.48.21 node1
192.168.48.22 node2
192.168.48.23 node3`

通过 yum 下载各种工具，`yum search ifconfig`，然后下载提示的软件包

## Zookeeper

zookeeper 的包需要上传，解压完成后修改 `/etc/profile.d/zk.sh` 文件，增加zk的环境变量

zk修改文件 `./conf/zoo.cfg`

修改 dataDir 为`dataDir=/usr/local/zookeeper.3.4.6/data`

末尾增加以下内容

```properties
server.0=node1:2888:3888
server.1=node2:2888:3888
server.2=node3:2888:3888
```

zookeeper.cli 是以树形存储的结构，内容是键值对形式

#### 编写开机启动

在`/etc/rc.d/init.d/zookeeper`文件中编写以下内容

```bash
#!/bin/bash

#chkconfig:2345 20 90
#description:zookeeper
#processname:zookeeper

case $1 in
        start) /usr/local/lib/zookeeper-3.4.6/bin/zkServer.sh start;;
        stop) /usr/local/lib/zookeeper-3.4.6/bin/zkServer.sh stop;;
        status) /usr/local/lib/zookeeper-3.4.6/bin/zkServer.sh status;;
        restart) /usr/local/lib/zookeeper-3.4.6/bin/zkServer.sh restart;;
        *)  echo "require start|stop|status|restart" ;;
esac
```

编辑完后运行`chkconfig --add zookeeper`添加到开机启动，然后可通过`chkconfig --list zookeeper` 查看 zookeeper 运行状况

## Kafka集群搭建

下载解压 kafka

#### kafka 配置

```properties
# 修改broker.id，3个节点不同
broker.id=0/1/2
port=9092
host.name=192.168.48.21/22/23
advertised.host.name=192.168.48.21/22/23
log.dirs=/usr/local/kafka_xxx/kafka-logs
num.partitions=5
zookeeper.connection=192.168.48.21:2181,192.168.48.22:2181,192.168.48.23:2181
```

配置完后创建 logs 目录

然后后台启动 kafka：`nohup xxx &`

#### 生产者、消费者代码

> kafka手动提交需要将 offset + 1


















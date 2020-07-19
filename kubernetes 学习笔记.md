# kubernetes 学习笔记
## kubernetes 概述
#### 特点
* 以 Pod（容器组）为基本的编排和调度单元以及声明式的对象配置模型（控制器、configmap、secret 等）
* 资源配额与分配管理
* 健康检查、自愈、伸缩与滚动升级
#### 选择 k8s
关于为何选择使用一个组件，可以从以下两个角度来看
1. 从软件生态的角度（市场）来看
2. 从云应用角度（实际）看
#### kubernetes 依赖的组件
* 仓库：Atomic Registry、Docker Registry 等
* 网络：OpenvSwitch 和智能边缘路由等
* 监控：Heapster、Kibana、Hawkular 和 Elastic
* 安全：LDAP、SELinux、 RBAC 与 支持多租户的 OAUTH
* 自动化：通过 Ansible 的 playbook 进行集群的安装和生命周期管理
* 服务：大量事先创建好的常用应用模板
#### kubernetes 相关网站
1. https://kubernetes.io/
2. https://github.com/kubernetes
3. https://zhuanlan.zhihu.com/p/30355957
4. https://www.infoq.cn/article/U2a_7ekuvhmb7dSNp27V
5. https://github.com/kubernetes-retired/kubeadm-dind-cluster
#### kubernetes 架构
可以看这个地址 https://www.shiyanlou.com/courses/1457/learning/?id=15764
用户命令行和 UI 界面通过 API 与 k8s master 节点通信，k8s master 再与相关 node 进行通信
#### k8s master 节点
master 是控制节点，负责调度管理整个系统，具有以下组件
* k8s API Server：系统资源操作的入口，提供认证、授权、访问控制、API 注册和发现等，外部和内部组件可以通过 REST API 接口的方式获取数据信息
* k8s Scheduler：负责集群中的资源调度，为 新建的 pod 分配机器
* k8s Controller Manager：负责各种控制器
* Etcd：存储中间件，用于保存集群中所有的网络配置和对象的状态信息。Etcd 世高可用的键值存储系统，通过 Raft 一致性算法处理日志复制来保证强一致性。k8s 中的重要数据都持久化到 Etcd 中，所有架构中的各个组件都是*无状态*的。
#### node 节点
node 节点是运行节点，主要用于运行管理业务的容器，组件如下
* kubelet：负责容器生命周期
* kubernetes Proxy：负责为 Pod 创建代理服务，从 master 中的 API Server 获取所有 Service，然后创建代理服务。也就是集群内部的服务发现和负载均衡
* Kubernetes Container Runtime：容器运行时，使用多种容器，最常使用 Docker 服务（CRI）
#### 常见资源类型
* CRI(Container Runtime Interface)：容器运行时接口，提供计算资源。
* CNI(Container Network Interface)：容器网络接口，提供网络资源。它由一组配置 Linux 容器的网络接口的规范和库组成，同时还包含了一些插件。
* CSI(Container Storage Interface)：容器存储接口，提供存储资源。
#### k8s 对象和核心概念
##### API 对象
k8s 中大部分概念都可看做是一种资源对象，通过 *kubectl* 命令行工具对其进行增删改查等操作病并存在 Etcd 中。
一个 API 对象在 Etcd 中的完整资源路径是由：Group(API 组)、Version(API 版本) 和 Resource(API 资源类型) 构成。
每个 API 对象有 3 大类属性：
* metadata：元数据。用来标识 API 对象，每个对象都至少有 3 个元数据：namespace、name 和 uid，还可以使用标签 labels 标识和匹配不同的对象。
* spec：规范。描述用户期望集群中的分布式系统达到的理想状态。
* status：状态。描述系统当前达到的状态。在任何时候，Kubernetes 会管理对象使它的实际状态和期望状态相一致。
##### Pod
Pod 是 k8s 中最重要的核心概念，其他的对象都是在管理、暴露 Pod 或是被 Pod 使用












## 创建 yaml

创建 mysql 的 ReplicationController  的 yaml

```yaml
apiVersion: v1 # 这是稳定长期使用的 API 版本，具体资源对象的版本可以参考“推荐阅读”
kind: ReplicationController # 副本控制器 RC
metadata: # 元数据
  name: mysql # 定义这个 rc 的名称为 mysql，它具有全局唯一性
spec: # 规范
  replicas: 1 # pod 副本的期待数量，这里为 1，表示当环境中没有符合条件要求的 pod 数量时需要创建一个，如果符合要求的 pod 数量大于一个就要删除多余的 pod，始终要保持环境中只有一个 pod 符合要求。
  selector: # 标签选择器
    app: mysql # 符合要求的 pod 都必须要有这个标签 app:mysql
  template: # 创建 pod 副本的模板
    metadata: # 创建的 pod 副本拥有的元数据
      labels: # 创建的 pod 副本的标签
        app: mysql # 对应 RC 的标签选择器，保证创建的 pod 副本符合 RC 的选择器范围
    spec: # 定义 pod 副本中运行的容器的详细信息
      containers: # 容器信息
      - name: mysql # 容器的名字
        image: mysql:5.7 # 容器中运行的镜像
        ports: # 容器对外暴露的端口
        - containerPort: 3306 # 容器的端口 3306
        env: # 容器中的环境变量
        - name: MYSQL_ROOT_PASSWORD # 设置环境变量 MYSQL_ROOT_PASSWORD="123456"
          value: "123456"
```

上面展示了一本的 yaml 文件必备的4个属性

- apiVersion（API 版本）
- kind（资源类型）
- metadata（原数据）
- spec（规范）

```shell
# 通过 yaml 文件创建
shiyanlou:~/ kubectl create -f mysql-rc.yaml

# 查看已经创建的资源对象
shiyanlou:~/ kubectl get rc
NAME    DESIRED   CURRENT   READY   AGE
mysql   1         1         0       3m35s
# 查看环境中的 pod
shiyanlou:~/ kubectl get pods
NAME          READY   STATUS             RESTARTS   AGE
mysql-tq2v4   0/1     ImagePullBackOff   0          4m12s

# 查看具体构建过程
# 其中 Events 记录了主要的事件，包括构建失败
shiyanlou:~/ kubectl describe pod mysql-3244
```

-f 表示指定文件名

在创建成功运行着 MySQL 的 pod 之后，还需要创建一个与之外应的 Service，这样才能让其它的 pod 可以访问 MySQL 应用提供的服务。

Service 被创建之后，Kubernetes 会给它分配一个虚拟的 Cluster IP，并且**在 Service 的整个生命周期中，这个 Cluster IP 都不会改变**，<u>这样的好处就是，pod 可能会随时发生变化（由于某些意外情况被销毁然后重建），但是我们的 Service 始终是确定不变的，在 Kubernetes 集群内部通过服务名和暴露出来的端口就可以稳定的访问某一个服务而不需要管隐藏在服务背后的 pod 的变化</u>。

通过 yaml 创建 service

```yaml
apiVersion: v1 # API 版本号，使用稳定版：v1
kind: Service # 资源对象类型
metadata: # 元数据
  name: mysql # Service 的名称为：mysql，这个名称具有全局唯一性
spec: # 规范
  ports: # 对外暴露出的端口
    - port: 3306 # 这里指定为 3306 端口
  selector: # 标签选择器
    app: mysql # 服务对应的 pod 是具有 app=mysql 标签的这些
```

创建 tomcat rc 的 yaml

```yaml
apiVersion: v1
kind: ReplicationController
metadata:
  name: myweb # rc 的名称为 myweb
spec:
  replicas: 1 # 只需要创建一个 pod 副本
  selector:
    app: myweb # 选择那些具有 app=myweb 标签的 pod
  template:
    metadata:
      labels:
        app: myweb # 创建的 pod 副本拥有的标签为 app=myweb
    spec:
      containers:
      - name: myweb # 容器名为 myweb
        image: kubeguide/tomcat-app:v1
        ports:
        - containerPort: 8080 # 容器暴露出来的端口为 8080 端口
        env: # 环境变量
        - name: MYSQL_SERVICE_HOST # 在容器中使用环境变量的值 MYSQL_SERVICE_HOST=mysql 来连接服务
          value: 'mysql'
        - name: MYSQL_SERVICE_PORT # 通过环境变量指定服务的端口
          value: '3306'
```

创建 myweb 对应的 Service，需要注意的是一方面 myweb 服务需要和 MySQL 服务进行通信，这里我们可以通过服务指定开放 8080 端口进行通信，另一方面，通过外部的网络也需要能够访问 myweb 服务才可以，而创建的 <u>service 对应的 ClusterIP 是虚拟的只能用于集群内部，外部网络是无法使用的</u>，在这里可以考虑使用节点的 IP 地址作为固定 IP 进行访问，因为<u>节点的 IP 地址是固定不会变化且真实存在的</u>。

创建 myweb 对应的 yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myweb # 服务的名称为 myweb
spec:
  type: NodePort # 这里使用 NodePort 开启外网访问模式
  ports:
    - port: 8080 # myweb 服务在集群内部开放了 8080 端口用于连接
      nodePort: 30001 # 对于外网连接，开放了节点的 30001 端口
  selector:
    app: myweb # myweb 服务包含的是具有 app=myweb 标签的 pod
```

```shell
$ kubectl get svc
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
kubernetes   ClusterIP   10.96.0.1        <none>        443/TCP          8d
mysql        ClusterIP   10.100.191.114   <none>        3306/TCP         11m
myweb        NodePort    10.107.104.225   <none>        8080:30001/TCP   32s
```

根据输出可以发现，myweb 服务的类型是 **NodePort**，它在集群内部的虚拟 IP 为：10.107.104.225，在集群内部暴露的是 8080 端口，而对于外网访问是暴露的各个节点的 30001 端口。

删除对应 rc 和 svc

```shell
kubectl delete rc mysql myweb

kubectl delete svc mysql myweb
```












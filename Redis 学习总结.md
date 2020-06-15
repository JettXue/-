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

## 数据类型

**List 操作**

**lpop** 操作在数组中没有值时会**阻塞**等待

负数表示倒数下标

```shell
insertlinsert key BEFORE|AFTER pivot value
```

**RPOPLPUSH source destination:**

移除 source 的队尾元素添加到 destination 的队首并返回

**zset 操作**zadd key score 1 member1 [score2 member2]添加或更新 member 的分数member 不可重复，设置相同的 member 将更新对应 scorescore 可重复根据 score 进行排序**默认升序**


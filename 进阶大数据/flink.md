

| Transformation | Description                                                  |
| -------------- | ------------------------------------------------------------ |
| Map            | **返回转换的一个元素**<br />读入一个元素，返回转换后的一个元素。<br />一个把输入流转换中的数值翻倍的 map function:<br />`dataStream.map { x => x * 2 }` |
| FlatMap        | **返回多个元素**<br />读入一个元素，返回转换后的0个、1个或多个元素。<br />将子句切分成单词的 flatmap function: <br />`dataStream.flatMap { str => str.split(" ") }` |
| Filter         | **过滤**<br />对读入的每个元素执行 boolean 函数，并保留返回 true 的元素<br />过滤零值的 filter<br />`dataStream.filter { _ != 0}` |
| MapPartition   | **单个函数调用中转换并行分区**<br />函数将分区作为一个“迭代器”，并可以产生任意数量的结果值。每个分区中的元素数量取决于并行度和以前的操作。<br />`data.mapPartition { in => Some(in.size) }` |
| Reduce         | **合并**<br />将当前元素与上一个 reduce 后的值进行合并，再返回新合并的值<br />`data.reduce { _ + _ }` |


# HUST-Java-Experiments
Hust J experiment in Spring 2019. 华中科技大学Java实验

基础设计基于[@husixu1](https://github.com/husixu1)的repo: [HUST-Homeworks/JavaExperiment/](https://github.com/husixu1/HUST-Homeworks/tree/master/JavaExperiment)，在外观和功能上进行了**迭代**。


## 功能

原版：
- 病人基础挂号
- 医生查看挂号和收入信息
- 病人挂号多选框支持拼音、前缀匹配
- 医生界面表格支持时间筛选
  
增加：
- 病人多选框支持子序列匹配（e.g: “华中大”可匹配“华中科技大学”）
- 医生界面表格数据获取异步加载。（2300条数据从服务器端拿下来的时间测试800ms+）
- 支持病人注册
- 数据库修改pid为int with zero fill，从而可以使用自增锁，避免pid申请重复
- 对隔日挂号单价格冲突时间使用事务回滚来预防
- 医生界面可以查看收入、挂号次数统计图，横轴可在年月周日等跨度自由调节。
- 界面可以查看时间（时间为从SQL服务端获取，防止客户端时间不一致）
- 单例模式实现换为内部类
- 代码权限重构，前后分离

## 依赖

原版：
- JFoenix
- JDBC

增加：
- JFreeChart
- Beautyeye

## 文件

- lib/*：依赖
- out/*：编译输出
- res/*：图片资源
- src/*：源码
- DBbackup.sql：数据库备份文件（程序没有建表相关逻辑，所以需要先进行导入）

>1. create a database with name "java_lab2" 
>2. create a user named "java" with password set to "javajava", and grant all privileges on java_lab2 to it.


## TODO
- SQL CURD的时间维度都通过```now()```来从服务端获取。
- 挂号id修改为类pid的自增方式。
- 图表内嵌到FX面板（LineChart）
- 增加修改密码逻辑
- 偶尔在服务器端长时间获取表单的时候会有bug，时间紧，没注意复现。

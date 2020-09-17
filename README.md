### 本系统使用springboot框架实现社区论坛的基本功能

SpringBoot版本： `2.3.3.RELEASE`；

MySQL版本： `8.0.16`；

Redis版本：`3.2.100`；

kafka版本：`kafka_2.13-2.6.0`；

elasticsearch版本：`7.6.2`；

application.properties配置文件；

```properties
# 修改数据库用户名和密码；
spring.datasource.username=
spring.datasource.password=

# 邮箱配置，开启SMTP服务
spring.mail.username=
spring.mail.password=
```

使用时需要开启kafka、zookeeper和elasticsearch后台进程；






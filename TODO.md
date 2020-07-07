## LibergCoder插件

## 待实现

- 目前interfaces中，声明方法前面必须加public，否则不能正常解析。
- 初始化完成之后，如何优雅地reload项目文件。
- build entity/interface之后，如何优雅地进行reload。
- 缓存系统还需要完成。
- 欠缺测试用例。







## 已实现功能

- pom.xml自动添加fastjon依赖

  ```xml
  <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>fastjson</artifactId>
      <version>1.2.62</version>
  </dependency>
  ```

- pom.xml自动添加mysql依赖

  ```xml
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```
- application.properties中自添加默认配置，如果缺失的话

```properties
server.port=8080
spring.application.name=$PROJECT_NAME$
spring.datasource.url=jdbc:mysql://localhost:3306/
spring.datasource.name=demo_data
spring.datasource.username=root
spring.datasource.password=
```

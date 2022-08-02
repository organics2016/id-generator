[maven-img]: https://img.shields.io/maven-central/v/ink.organics/id-generator

[license-img]: https://img.shields.io/github/license/organics2016/id-generator

[downloads-img]: https://img.shields.io/github/downloads/organics2016/id-generator/total

[github]: https://github.com/organics2016/id-generator

[id-generator]: https://mvnrepository.com/artifact/ink.organics/id-generator

[![][license-img]][github]
[![][maven-img]][id-generator]
[![][downloads-img]][github]

### [English](./README.md) | [中文](./README_ZH.md)

# ID Generator

一个简单高效的有序 ID 生成器，可以帮助您轻松生成可读且美观的业务主键。尤其是当您遇到复杂的框架配置和许多分布式协调服务时，您可以试试看这个。

## Feature

- 完备的雪花ID实现 [Snowflake ID](https://en.wikipedia.org/wiki/Snowflake_ID)
- 突破雪花ID在 1 毫秒内只能生成 4095 个有效ID的并发限制。
- 干净的配置接口
- 不依赖任何服务
- 支持Java17

## Getting started

- Step1. 添加这个dependency到 `pom.xml`

```xml

<dependency>
    <groupId>ink.organics</groupId>
    <artifactId>id-generator</artifactId>
    <version>3.0.1</version>
</dependency>
```

- Step2. 初始化一个Generator。例如像在Spring中

```java
import ink.organics.idgenerator.generator.Generator;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public Generator idGenerator() {
        return SnowflakeGenerator.build(
                "server_1",      // Current service identifier
                List.of("server_1", "server_2")); // All services identifier.
    }
}
```

- 你需要注意
    - 第一个参数. **您需要确保 当前服务标识ID 在集群中是全局唯一的。更准确说，在集群中的所有JVM进程中是唯一的。**
    - 第二个参数. **需要保证List的元素和元素的顺序在所有服务中是一致的，最大不能超过1023。**
    - 例如 相同的代码在不同的进程中

```java

@Configuration
public class ApplicationConfiguration {
    @Bean
    public Generator idGenerator() {
        return SnowflakeGenerator.build(
                "server_1",
                List.of("server_1", "server_2"));
    }
}
```

```java

@Configuration
public class ApplicationConfiguration {
    @Bean
    public Generator idGenerator() {
        return SnowflakeGenerator.build(
                "server_2",
                List.of("server_1", "server_2"));
    }
}
```

- Step3. 可以在任何地方使用

```java
import ink.organics.idgenerator.generator.Generator;

@SpringBootTest
public class SpringTest {

    @Autowired
    private Generator generator;

    @Test
    public void test1() {
        long id = generator.next();

        assertThat(id).isGreaterThan(76976953847971840L);
    }
}
```

## More Details

### 数字太丑了我需要更有意义的 ID

1. 初始化一些Decorator。例如像在Spring中

```java
import ink.organics.idgenerator.IDGeneratorManager;
import ink.organics.idgenerator.decorator.Decorator;
import ink.organics.idgenerator.decorator.impl.StringDecoratorRule;
import ink.organics.idgenerator.generator.Generator;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public IDGeneratorManager idGeneratorManager() {
        return IDGeneratorManager.getInstance().init(
                Decorator.builder()     // Build a decorator
                        .generatorId("generatorId_1")  //  The decorator need a id
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .decoratorRule(StringDecoratorRule.builder().prefix("QQQ").autoComplete(true).build())  //  Set some rules
                        .build(),

                Decorator.builder()
                        .generatorId("generatorId_2")
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .decoratorRule(StringDecoratorRule.builder().postfix("WWW").autoComplete(false).build())
                        .build()
        );
    }
}
```

2. 可以在任何地方使用

```java
import ink.organics.idgenerator.IDGenerator;
import ink.organics.idgenerator.generator.Generator;

@SpringBootTest
public class SpringDemoTest {
    @Test
    public void test2() {
        String generatorId_1 = IDGenerator.nextToString("generatorId_1");
        assertThat(generatorId_1).startsWith("QQQ");

        String generatorId_2 = IDGenerator.nextToString("generatorId_2");
        assertThat(generatorId_2).endsWith("WWW");
    }
}
```

---

### 集成到 Spring Data JPA

1. 初始化一个Generator

```java

@Configuration
public class ApplicationConfiguration {
    @Bean
    public IDGeneratorManager idGeneratorManager() {
        return IDGeneratorManager.getInstance().init(
                Decorator.builder()     // Build a decorator
                        .generatorId("generatorId_1")  //  The decorator need a id
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .decoratorRule(StringDecoratorRule.builder().prefix("QQQ").autoComplete(true).build())  //  Set some rules
                        .build());
    }
}
```

2. 创建一个 Hibernate IdentifierGenerator

```java
import ink.organics.idgenerator.IDGenerator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class MyGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return IDGenerator.nextToString("generatorId_1");
    }
}
```

3. 在你的Entity上使用

```java

@Data
@Entity(name = "user_t")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(generator = "my-generator")
    @GenericGenerator(name = "my-generator", strategy = "com.example.MyGenerator")
    private String id;
    private String name;
    private Integer age;
    private String email;
}
```

4. 试试看

```java

@SpringBootTest
public class SpringDemoTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test3() {
        User user = new User();
        userRepository.saveAndFlush(user);

        userRepository.findAll().forEach(user1 -> {
            System.out.println(user.getId());
            assertThat(user1.getId()).startsWith("QQQ");
        });
    }
}
```

## Q&A

- 没有Spring可以用吗？
- 是的，只需在正确的位置初始化即可。
  <br>
  <br>
- 为什么我需要维护服务列表和服务标识ID？
- 没有注册中心很难保证唯一性，但大多数系统不需要引入复杂的注册中心。无论哪种情况，我们都希望将选择权留给用户。
  <br>
  <br>
- 怎样突破并发限制？
- 当 1 毫秒内产生超过 4095 个 ID 时，程序会借用下一 1 毫秒，不会超过 1 秒。
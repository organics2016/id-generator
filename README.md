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

A simple and efficient ordered ID generator, can help you easily generate readable and beautiful business primary keys.
Especially when if you are suffering from complex framework configuration and many distributed coordination services,
you can try it.

## Feature

- Complete [Snowflake ID](https://en.wikipedia.org/wiki/Snowflake_ID) Implementation
- Breaking through the concurrency limit that Snowflake ID can only generate 4095 valid IDs in 1 millisecond.
- Clean configuration interface.
- Option to not depend on any service.
- Support Java17

## Getting Started

- Step1. Add this dependency to your `pom.xml`

```xml

<dependency>
    <groupId>ink.organics</groupId>
    <artifactId>id-generator</artifactId>
    <version>3.0.2</version>
</dependency>
```

- Step2. Initialize a generator. like in the Spring.

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

- You need to pay attention
    - First parameter. **You need to ensure that the current service identifier is globally unique in your cluster. More
      precisely, it is unique among all JVM processes in the cluster.**
    - Second parameter. **You need to ensure that List's elements and the order of the elements are the same
      in all services, and the maximum size cannot exceed 1023.**
    - E.g The same code in different processes.

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

- Step3. Use it anywhere.

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

## Q&A

### @Autowired is too troublesome, and I don't have Spring.

1. Find a place to initialize.

```java
import ink.organics.idgenerator.IDGeneratorManager;
import ink.organics.idgenerator.decorator.Decorator;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;

public class ApplicationConfiguration {

    public void initIdGeneratorManager() {
        IDGeneratorManager.getInstance()
                .init(Decorator.builder()     // Build a decorator
                        .generatorId("generatorId_1")  //  The decorator need a id
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .build());
    }
}
```

2. Use it anywhere.

```java
import ink.organics.idgenerator.generator.Generator;

public class DemoTest {
    @Test
    public void test2() {
        long id = IDGenerator.next("generatorId_1"); // Need decorator id
        assertThat(id).isGreaterThan(76976953847971840L);
    }
}
```

---

### The numbers are so ugly. I need more meaningful ids.

1. Initialize some decorator.

```java
import ink.organics.idgenerator.IDGeneratorManager;
import ink.organics.idgenerator.decorator.Decorator;
import ink.organics.idgenerator.decorator.impl.StringDecoratorRule;
import ink.organics.idgenerator.generator.Generator;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;

public class ApplicationConfiguration {

    public void initIdGeneratorManager() {
        IDGeneratorManager.getInstance().init(
                Decorator.builder()     // Build a decorator
                        .generatorId("generatorId_1")  //  The decorator need a id
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .decoratorRule(StringDecoratorRule.builder().prefix("QQQ").autoComplete(true).build())  //  Optional. Set some rules
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

2. Use it anywhere.

```java
import ink.organics.idgenerator.IDGenerator;
import ink.organics.idgenerator.generator.Generator;

public class DemoTest {
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

### Integrate with Spring Data JPA

1. Initialize a generator

```java

@Configuration
public class ApplicationConfiguration {
    @Bean
    public IDGeneratorManager idGeneratorManager() {
        return IDGeneratorManager.getInstance()
                .init(Decorator.builder()     // Build a decorator
                        .generatorId("generatorId_1")  //  The decorator need a id
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .decoratorRule(StringDecoratorRule.builder().prefix("QQQ").autoComplete(true).build())  //  Optional. Set some rules
                        .build());
    }
}
```

2. Create a Hibernate IdentifierGenerator

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

3. Use it to your entity

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

4. Test it.

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

---

### Why I need to maintain service list and service identifier?

- It is difficult to guarantee unique without registration service, but most systems not need to introduce complex
  registration service. If you have many services or need dynamic scaling, you can create a Redis-based Generator as
  follows.

- **Note: You can only choose one.**

1. Simple, does not depend on any service, does not support dynamic scaling. Suitable for where the number of servers is
   fixed and relatively small.

```
SnowflakeGenerator.build("server_1", List.of("server_1", "server_2"));
```

2. High level, Requires Redis. Suitable for container drift or dynamic scaling. E.g Kubernetes | Docker | Serverless

```
// "redis://:password@host:port/database"

SnowflakeGenerator.build("redis://127.0.0.1:6379/0");
```

---

### How do breaking through the concurrency limit?

- When more than 4095 IDs are generated in 1 millisecond, the program will borrow the next 1 millisecond, and it will
  not exceed 1 second.
[maven-img]: https://img.shields.io/maven-central/v/ink.organics/id-generator

[id-generator]: https://mvnrepository.com/artifact/ink.organics/id-generator

[![][maven-img]][id-generator]

# ID Generator

The new version is doing the final test case, the old version can [click here](./README_LINA.md)

## Profile

A simple and efficient ordered ID generator. Can help you easily generate readable and beautiful business primary keys.
Especially when if you are suffering from complex framework configuration and many distributed coordination services,
you can try it.

## Feature

- Complete [Snowflake ID](https://en.wikipedia.org/wiki/Snowflake_ID) Implementation
- Breaking through the concurrency limit that Snowflake ID can only generate 4095 valid IDs in 1 millisecond.
- Clean configuration interface
- Does not depend on any service
- Support Java17

## Getting started

- Step1. Add this dependency to your `pom.xml`

```xml

<dependency>
    <groupId>ink.organics</groupId>
    <artifactId>id-generator</artifactId>
    <version>3.0.0</version>
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

## More Details

#### The numbers are so ugly. I need more meaningful ids.

1. Initialize some decorator. like in the Spring.

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

2. Use it anywhere.

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

#### Integrate with Spring Data JPA

1. Initialize a generator

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

## Q&A

- Can it be used without Spring?
- Yes, just initialize in the right place.
  <br>
  <br>
- Why I need to maintain service list and service identifier?
- It is difficult to guarantee unique without registration service, but most systems not need to introduce complex
  registration service. In either case, we want to leave the choice to the user.
  <br>
  <br>
- How do breaking through the concurrency limit?
- When more than 4095 IDs are generated in 1 millisecond, the program will borrow the next 1 millisecond, and it will
  not exceed 1 second.
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
  - Here you need to note that **the current service identifier must exist in all service lists.**

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

- Step3. Use it anywhere.

```java
import ink.organics.idgenerator.generator.Generator;

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
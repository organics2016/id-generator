package ink.organics.test.spring.config;

import ink.organics.idgenerator.IDGeneratorManager;
import ink.organics.idgenerator.decorator.Decorator;
import ink.organics.idgenerator.decorator.impl.StringDecoratorRule;
import ink.organics.idgenerator.generator.Generator;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public Generator idGenerator() {
        return SnowflakeGenerator.build(
                "server_1",           // Current service identifier
                List.of("server_1", "server_2"));  // All services identifier
    }

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

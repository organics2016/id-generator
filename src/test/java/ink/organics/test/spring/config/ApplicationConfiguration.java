package ink.organics.test.spring.config;

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
}

package ink.organics.test.spring;

import ink.organics.idgenerator.generator.Generator;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;
import ink.organics.test.spring.model.repository.UserRepository;
import ink.organics.test.spring.model.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

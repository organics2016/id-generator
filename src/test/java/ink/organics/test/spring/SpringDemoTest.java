package ink.organics.test.spring;

import ink.organics.idgenerator.IDGenerator;
import ink.organics.idgenerator.generator.Generator;
import ink.organics.test.spring.model.entity.User;
import ink.organics.test.spring.model.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SpringDemoTest {

    @Autowired
    private Generator generator;

    @Test
    public void test1() {
        long id = generator.next();
        assertThat(id).isGreaterThan(76976953847971840L);
    }

    @Test
    public void test2() {
        String generatorId_1 = IDGenerator.nextToString("generatorId_1");
        assertThat(generatorId_1).startsWith("QQQ");

        String generatorId_2 = IDGenerator.nextToString("generatorId_2");
        assertThat(generatorId_2).endsWith("WWW");
    }

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

package ink.organics.test.spring;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JPABootApplication {

    public static void main(String[] args) {
        SpringApplication.run(JPABootApplication.class, args);
    }
}

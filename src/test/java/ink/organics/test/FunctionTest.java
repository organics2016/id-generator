package ink.organics.test;

import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


public class FunctionTest {


    @Test
    public void test() {

        SnowflakeGenerator snowflakes = SnowflakeGenerator.build("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));

        final ExecutorService executor = Executors.newFixedThreadPool(12);

        Map<Long, Object> map = new ConcurrentHashMap<>();

        for (int t = 0; t < 100; t++) {
            executor.submit(() -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < 10000; i++) {
                    map.put(snowflakes.next(), "");
                }
                long end = System.currentTimeMillis();
                System.out.println(Thread.currentThread().getId() + " : " + (end - start));
            });
        }

        try {
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdown();
                assertThat(map.size()).isEqualTo(100 * 10000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

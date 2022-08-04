package ink.organics.test;

import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FunctionTest {

    @Test
    public void test() throws NoSuchAlgorithmException {
//        System.out.println(Long.toBinaryString(-1L));
//        System.out.println(~(-1L << 10));
//        System.out.println(Long.toBinaryString(Long.parseLong("111111111111", 2)));
//        System.out.println(Long.parseLong("0111111111111111111111111111111111111111111111111111111111111111", 2));

    }


    @Test
    public void redis1() throws InterruptedException {
        SnowflakeGenerator generator = SnowflakeGenerator.build("redis://127.0.0.1:6379/0");

        Map<Long, Object> map = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();
        for (long l = 0; l < 10000000; l++) {
            map.put(generator.next(), "");
            Thread.sleep(10 * 1000);
        }
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " : " + (end - start));

        assertThat(map.size()).isEqualTo(10000000);
    }

    // Jedis jedis = new Jedis("redis://:password@host:port/database");
    @Test
    public void redis() {
        SnowflakeGenerator generator = SnowflakeGenerator.build("redis://127.0.0.1:6379/0");
        testSnowflakeGenerator(generator);
    }

    @Test
    public void def() {
        SnowflakeGenerator generator = SnowflakeGenerator.build("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));
        testSnowflakeGenerator(generator);
    }

    public void testSnowflakeGenerator(SnowflakeGenerator generator) {

        final ExecutorService executor = Executors.newFixedThreadPool(12);

        Map<Long, Object> map = new ConcurrentHashMap<>();

        for (int t = 0; t < 1000; t++) {
            try {
                executor.submit(() -> {
                    long start = System.currentTimeMillis();
                    for (int i = 0; i < 10000; i++) {
                        map.put(generator.next(), "");
                    }
                    long end = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName() + " : " + (end - start));
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("error : ", e);
            }
        }

        try {
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdown();
                assertThat(map.size()).isEqualTo(1000 * 10000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

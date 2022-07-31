import ink.organics.lina.IDGenerator;
import ink.organics.lina.IDGeneratorManager;
import ink.organics.lina.decorator.Decorator;
import ink.organics.lina.decorator.impl.StringDecoratorRule;
import ink.organics.lina.generator.impl.SnowflakeGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class RunTest {

    @Test
    public void test() {
        System.out.println(String.format("%064d", 41111));
    }

    @Test
    public void test1() {

//        IDGeneratorManager.(
//                new LinaConfig("ZCKP", new StringDecoratorRule().start(10000).autoComple(true).maxBit(10)),
//                new LinaConfig("ZCZY", new StringDecoratorRule().start(10000).autoComple(true).maxBit(10)))
////                .boot(new RedisCodeDao(new JedisConfig("192.168.0.99", 6379, "123456").getJedisPool()))
//                .boot(new DefaultCodeDao(classpath));

        IDGeneratorManager.getInstance().init(
                Decorator.builder()
                        .generatorId("ZCKP")
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                        .decoratorRule(StringDecoratorRule.builder().prefix("ZCKP").autoComplete(true).build())
                        .build(),
                Decorator.builder()
                        .generatorId("ZCZY")
                        .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
//                        .decoratorRule(StringDecoratorRule.builder().prefix("ZCZY").autoComplete(false).build())
                        .build()
        );

        String xxx = IDGenerator.nextToString("ZCKP");

        System.out.println(xxx);

        long aaa = IDGenerator.next("ZCZY");

        System.out.println(aaa);
    }

    @Test
    public void test2() {
        //        System.out.println(Long.toBinaryString(-1));
//        System.out.println(Long.parseLong("0111111111111111111111111111111111111111111111111111111111111111", 2));
//        System.out.println(Long.parseLong("011111111111111111111111111111111111111111", 2));
//        System.out.println(Long.parseLong("01111111111", 2));
//        System.out.println(Long.parseLong("0111111111111", 2));

        SnowflakeGenerator snowflakes = SnowflakeGenerator.build("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));
        SnowflakeGenerator snowflakes2 = SnowflakeGenerator.build("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));
        SnowflakeGenerator snowflakes3 = SnowflakeGenerator.build("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));
        SnowflakeGenerator snowflakes4 = SnowflakeGenerator.build("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));

        final ExecutorService executor = Executors.newFixedThreadPool(12);

        Map<Long, Object> map = new ConcurrentHashMap<>();

        for (int t = 0; t < 100; t++) {

            executor.submit(() -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < 100; i++) {
                    map.put(snowflakes.next(), "");
                }
                long end = System.currentTimeMillis();
                System.out.println(Thread.currentThread().getId() + " : " + (end - start));
            });

        }

        try {
            while (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdown();
                System.out.println(map.size() == (100 * 100));

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

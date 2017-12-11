import net.vkits.platform.lina.LinaConsole;
import net.vkits.platform.lina.LinaServer;
import net.vkits.platform.lina.config.LinaConfig;
import net.vkits.platform.lina.dao.impl.DefaultCodeDao;
import net.vkits.platform.lina.dao.impl.RedisCodeDao;
import net.vkits.platform.lina.redis.JedisConfig;
import net.vkits.platform.lina.rule.impl.LinaRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王汗超 on 2017/4/6.
 */
public class RunTest {


    public static void main(String[] args) {

        List<LinaConfig> l = new ArrayList<>();

        String classpath = RunTest.class.getClassLoader().getResource("").getFile();

        // ZCKP00001 格式固定可以按格式固定处理
        // 分类编码 1001  字典编码 00001  result 1001 + 00001
        LinaConsole c = LinaConsole.getInstance().init(
                new LinaConfig("ZCKP", new LinaRule().start(100).autoComple(true).prefix("SQ").maxBit(10)),
                new LinaConfig("ZCZY", new LinaRule().start(10000).autoComple(true).maxBit(10)))
                .boot(new DefaultCodeDao(classpath));

        test_1();
    }

    public static void test_3() {
        String xxx = LinaServer.nextCode("ZCKP");

        System.out.println(xxx);

        String aaa = LinaServer.nextCode("ZCZY");

        System.out.println(aaa);
    }

    public static void test_2() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
//            UUID.randomUUID().toString();
            String xxx = LinaServer.nextCode("ZCZY");
            System.out.println(xxx);
        }

        long end = System.currentTimeMillis();

        System.out.println("ms : " + (end - start));
    }


    public static void test_1() {

        new Thread(() -> {

            try {

                String[] s1 = new String[2];

                int i = 0;

                System.out.println("1 --->");

                Thread.sleep(5000);


                while (true) {

                    String xxx = LinaServer.nextCode("ZCKP");
                    if (i == 0) {
                        s1[1] = xxx;
                        i = 1;

                    } else {
                        s1[0] = s1[1];
                        s1[1] = xxx;
                    }

                    System.out.println(xxx);

                    if (s1[1].equals(s1[0])) {
                        throw new RuntimeException("!!!!");
                    }

//                    Thread.sleep(5);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();


        new Thread(() -> {

            try {

                String[] s1 = new String[2];

                int i = 0;

                System.out.println("2 --->");

                Thread.sleep(5000);

                while (true) {

                    String xxx = LinaServer.nextCode("ZCZY");
                    if (i == 0) {
                        s1[1] = xxx;
                        i = 1;

                    } else {
                        s1[0] = s1[1];
                        s1[1] = xxx;
                    }

                    System.out.println(xxx);

                    if (s1[1].equals(s1[0])) {
                        throw new RuntimeException("!!!!");
                    }

//                    Thread.sleep(10);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

    }
}

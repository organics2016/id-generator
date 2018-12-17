import ink.organics.lina.LinaConsole;
import ink.organics.lina.LinaServer;
import ink.organics.lina.config.LinaConfig;
import ink.organics.lina.dao.impl.TimeStampCodeDao;
import ink.organics.lina.rule.impl.TimeStampRule;

/**
 * Created by 王汗超 on 2017/4/6.
 */
public class RunTest {


    public static void main(String[] args) {

//        List<LinaConfig> l = new ArrayList<>();
//
//        String classpath = RunTest.class.getClassLoader().getResource("").getFile();
//
//        // ZCKP00001 格式固定可以按格式固定处理
//        // 分类编码 1001  字典编码 00001  result 1001 + 00001
//        LinaConsole c = new LinaConsole().init(
//                new LinaConfig("ZCKP", new LinaRule().start(10000).autoComple(true).maxBit(10)),
//                new LinaConfig("ZCZY", new LinaRule().start(10000).autoComple(true).maxBit(10)))
////                .boot(new RedisCodeDao(new JedisConfig("192.168.0.99", 6379, "123456").getJedisPool()))
//                .boot(new DefaultCodeDao(classpath));


        new LinaConsole().init(
                new LinaConfig("ZCKP", new TimeStampRule()),
                new LinaConfig("ZCZY", new TimeStampRule())
        ).boot(new TimeStampCodeDao());


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

                    System.out.println("T1 :" + xxx);

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

                    String xxx = LinaServer.nextCode("ZCKP");
                    if (i == 0) {
                        s1[1] = xxx;
                        i = 1;

                    } else {
                        s1[0] = s1[1];
                        s1[1] = xxx;
                    }

                    System.out.println("T2 :" + xxx);

                    if (s1[1].equals(s1[0])) {
                        throw new RuntimeException("!!!!");
                    }

                    Thread.sleep(10);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

    }
}

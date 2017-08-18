package net.vkits.platform.lina;


import net.vkits.platform.lina.service.Consumer;
import net.vkits.platform.lina.service.Producer;

/**
 * Created by 王汗超 on 2017/4/1.
 */
public class LinaServer {


    private static final Consumer consumer;

    private static final Producer producer;

    static {
        consumer = Consumer.getInstance();

        producer = Producer.getInstance();
    }


    private LinaServer() {
    }


    public static String nextCode(String groupId) {
        if (!LinaConsole.isInit())
            throw new RuntimeException("LinaServer is not init");

        String code = consumer.take(groupId);
        producer.produce(groupId, code);

        return code;
    }
}

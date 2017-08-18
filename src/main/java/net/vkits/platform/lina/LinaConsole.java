package net.vkits.platform.lina;


import net.vkits.platform.lina.dao.CodeDao;
import net.vkits.platform.lina.dto.LinaConfig;
import net.vkits.platform.lina.rule.Rule;
import net.vkits.platform.lina.service.Consumer;
import net.vkits.platform.lina.service.Producer;
import net.vkits.platform.lina.service.RuleConfig;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 王汗超 on 2017/4/1.
 */
public class LinaConsole {


    private static final LinaConsole LINA_CONSOLE = new LinaConsole();


    public static LinaConsole getInstance() {
        return LINA_CONSOLE;
    }

    private static boolean init = false;

    private static boolean boot = false;

    private RuleConfig ruleConfig;

    private Consumer consumer;

    private Producer producer;


    private LinaConsole() {
        ruleConfig = RuleConfig.getInstance();

        consumer = Consumer.getInstance();

        producer = Producer.getInstance();
    }


    public LinaConsole init(LinaConfig... linaConfigs) {
        if (init)
            return this;

        Map<String, Rule> map = new LinkedHashMap<>();
        LinaConfig[] arr = Arrays.copyOf(linaConfigs, linaConfigs.length);

        for (LinaConfig c : arr) {
            map.put(c.getGroupId(), c.getRule());
        }

        if (map.size() != arr.length) {
            throw new RuntimeException("Repeated groupId");
        }

        ruleConfig.init(map);

        consumer.init(ruleConfig.getAllGroupId());

        init = true;

        return this;
    }

    public LinaConsole boot(CodeDao dao) {
        if (boot)
            return this;

        if (!init)
            throw new RuntimeException("LinaServer is not init");

        producer.init(dao).start();

        boot = true;

        return this;
    }

    public LinaConsole boot() {
        return this.boot(null);
    }

    public static boolean isInit() {
        return init;
    }

    public static boolean isBoot() {
        return boot;
    }
}

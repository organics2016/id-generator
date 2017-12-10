package net.vkits.platform.lina;


import net.vkits.platform.lina.config.LinaConfig;
import net.vkits.platform.lina.config.RuleConfig;
import net.vkits.platform.lina.dao.CodeDao;
import net.vkits.platform.lina.rule.Rule;

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

    private final RuleConfig ruleConfig;


    private LinaConsole() {
        ruleConfig = RuleConfig.getInstance();
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

        init = true;

        return this;
    }

    public LinaConsole boot(CodeDao dao) {
        if (boot)
            return this;

        if (!init)
            throw new RuntimeException("LinaServer is not init");

        ruleConfig.getRuleMap().forEach((groupId, rule) -> {
            if (!dao.exists(groupId)) {
                dao.addCodeGroup(groupId, rule.getStart());
            }
        });

        LinaServer.setDao(dao);

        boot = true;

        return this;
    }

    public LinaConsole boot() {
        return this.boot(null);
    }

    public static boolean isInit() {
        return init && boot;
    }
}

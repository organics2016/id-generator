package net.vkits.platform.lina;


import net.vkits.platform.lina.config.LinaConfig;
import net.vkits.platform.lina.dao.CodeDao;
import net.vkits.platform.lina.rule.Rule;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by 王汗超 on 2017/4/1.
 */
public class LinaConsole {

    private boolean init = false;

    private boolean boot = false;

    private Map<String, Rule> ruleMap;

    private CodeDao codeDao;


    public LinaConsole init(LinaConfig... linaConfigs) {
        if (init)
            return this;

        this.ruleMap = Arrays.stream(linaConfigs)
                .collect(Collectors.toMap(LinaConfig::getGroupId, LinaConfig::getRule, (ov, nv) -> ov, ConcurrentHashMap::new));

        if (ruleMap.size() != linaConfigs.length) {
            throw new RuntimeException("Repeated groupId");
        }

        init = true;

        return this;
    }

    public LinaConsole boot(CodeDao dao) {

        if (!init)
            throw new RuntimeException("LinaServer is not init");

        if (boot)
            return this;

        this.codeDao = dao;

        this.codeDao.init(ruleMap);

        LinaServer.setConsole(this);

        boot = true;

        return this;
    }

    public LinaConsole boot() {
        return this.boot(null);
    }

    public boolean isInit() {
        return init && boot;
    }

    public Map<String, Rule> getRuleMap() {
        return ruleMap;
    }

    public CodeDao getCodeDao() {
        return codeDao;
    }
}

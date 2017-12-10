package net.vkits.platform.lina.config;


import net.vkits.platform.lina.rule.Rule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by 王汗超 on 2017/4/6.
 */
public class RuleConfig {


    private RuleConfig() {
    }

    private static final RuleConfig rule_config = new RuleConfig();


    public static RuleConfig getInstance() {
        return rule_config;
    }

    // 生产规则Map
    private final Map<String, Rule> ruleMap = new LinkedHashMap<>();


    public Set<String> getAllGroupId() {
        return ruleMap.keySet();
    }

    public Rule getRule(String groupId) {
        return ruleMap.get(groupId);
    }

    public Map<String, Rule> getRuleMap() {
        return ruleMap;
    }

    public void init(Map<String, Rule> ruleMap) {
        this.ruleMap.putAll(ruleMap);
    }
}

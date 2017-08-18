package net.vkits.platform.lina.dto;


import net.vkits.platform.lina.rule.Rule;

/**
 * Created by 王汗超 on 2017/4/5.
 */
public class LinaConfig {

    private String groupId;

    private Rule rule;

    public LinaConfig(String groupId, Rule rule) {
        this.groupId = groupId;
        this.rule = rule;
    }


    public String getGroupId() {
        return groupId;
    }

    public Rule getRule() {
        return rule;
    }
}

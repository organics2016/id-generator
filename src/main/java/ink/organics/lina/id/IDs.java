package ink.organics.lina.id;

import ink.organics.lina.rule.Rule;

import java.util.Map;



public interface IDs {


    void init(Map<String, Rule> ruleMap);


    long next(String groupId);
}

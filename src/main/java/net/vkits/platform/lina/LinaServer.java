package net.vkits.platform.lina;


import net.vkits.platform.lina.config.RuleConfig;
import net.vkits.platform.lina.dao.CodeDao;

/**
 * Created by 王汗超 on 2017/4/1.
 */
public class LinaServer {

    private static final RuleConfig ruleConfig;

    private static CodeDao dao;

    static {
        ruleConfig = RuleConfig.getInstance();
    }

    private LinaServer() {
    }

    public static void setDao(CodeDao dao) {
        LinaServer.dao = dao;
    }

    public static String nextCode(String groupId) {
        if (!LinaConsole.isInit())
            throw new RuntimeException("LinaServer is not init");

        long code = dao.nextCode(groupId);
        return ruleConfig.getRule(groupId).format(code);
    }
}

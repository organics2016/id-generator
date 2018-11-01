package net.vkits.platform.lina;


/**
 * Created by 王汗超 on 2017/4/1.
 */
public class LinaServer {

    private static LinaConsole console;

    private LinaServer() {
    }

    public static void setConsole(LinaConsole console) {
        LinaServer.console = console;
    }

    public static String nextCode(String groupId) {
        if (!console.isInit())
            throw new RuntimeException("LinaServer is not init");

        long code = console.getCodeDao().nextCode(groupId);
        return console.getRuleMap().get(groupId).format(code);
    }
}

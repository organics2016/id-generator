package ink.organics.lina;

public class IDGenerator {


    private static IDGeneratorManager generatorManager;

    private IDGenerator() {
    }

    static void setIDGeneratorManager(IDGeneratorManager idGeneratorManager) {
        generatorManager = idGeneratorManager;
    }

    private static void check() {
        if (!generatorManager.isInit())
            throw new RuntimeException("IDGeneratorManager is not init");
    }

    public static long next(String decoratorId) {
        check();
        return generatorManager.getGenerator(decoratorId).next();
    }


    public static String nextToString(String decoratorId) {
        check();
        return generatorManager.getDecorator(decoratorId).nextAndFormat().toString();
    }
}

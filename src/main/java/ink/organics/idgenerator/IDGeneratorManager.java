package ink.organics.idgenerator;


import ink.organics.idgenerator.decorator.Decorator;
import ink.organics.idgenerator.decorator.DecoratorRule;
import ink.organics.idgenerator.generator.Generator;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class IDGeneratorManager {

    private boolean init = false;

    private Map<String, Decorator> decoratorMap;

    private static final IDGeneratorManager instance = new IDGeneratorManager();

    private IDGeneratorManager() {
    }

    public static IDGeneratorManager getInstance() {
        return instance;
    }

    public IDGeneratorManager init(Decorator... decorators) {
        if (init)
            return this;

        this.decoratorMap = Arrays.stream(decorators)
                .collect(Collectors.toConcurrentMap(Decorator::getGeneratorId, d -> d));

        if (decoratorMap.size() != decorators.length) {
            throw new IllegalArgumentException("GeneratorId has repeating!");
        }

        IDGenerator.setIDGeneratorManager(this);

        init = true;

        return this;
    }

    public boolean isInit() {
        return init;
    }

    public Decorator getDecorator(String decoratorId) {
        return decoratorMap.get(decoratorId);
    }

    public Generator getGenerator(String decoratorId) {
        return getDecorator(decoratorId).getGenerator();
    }

    public DecoratorRule getDecoratorRule(String decoratorId) {
        return getDecorator(decoratorId).getDecoratorRule();
    }
}

package ink.organics.idgenerator.decorator;

import ink.organics.idgenerator.generator.Generator;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class Decorator {


    private final String generatorId;

    private final Generator generator;

    private final DecoratorRule decoratorRule;

    public long next() {
        return generator.next();
    }

    public Serializable format(long id) {
        return decoratorRule.format(id);
    }

    public Serializable nextAndFormat() {
        return format(next());
    }
}

package ink.organics.lina.decorator.impl;


import ink.organics.lina.decorator.DecoratorRule;
import lombok.Builder;


@Builder
public class StringDecoratorRule implements DecoratorRule {

    @Builder.Default
    private final String prefix = "";

    @Builder.Default
    private final String postfix = "";

    @Builder.Default
    private final boolean autoComplete = false;

    @Builder.Default
    private final int maxBit = 19;

    @Override
    public String format(long id) {

        String strId;
        if (this.autoComplete) {
            strId = String.format("%0" + this.maxBit + "d", id);
        } else {
            strId = String.valueOf(id);
        }

        return this.prefix + strId + this.postfix;
    }

}



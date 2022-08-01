package ink.organics.idgenerator.decorator;

import java.io.Serializable;


public interface DecoratorRule {

    Serializable format(long id);

}

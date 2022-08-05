package ink.organics.test;

import ink.organics.idgenerator.IDGenerator;
import ink.organics.idgenerator.IDGeneratorManager;
import ink.organics.idgenerator.decorator.Decorator;
import ink.organics.idgenerator.decorator.impl.StringDecoratorRule;
import ink.organics.idgenerator.generator.impl.SnowflakeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;


public class FunctionTest {

    @BeforeAll
    public static void init() {
        IDGeneratorManager.getInstance()
                .init(
                        Decorator.builder()     // Build a decorator
                                .generatorId("generatorId_1")  //  The decorator need a id
                                .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                                .decoratorRule(StringDecoratorRule.builder().prefix("QQQ").autoComplete(true).build())  //  Set some rules
                                .build(),

                        Decorator.builder()
                                .generatorId("generatorId_2")
                                .generator(SnowflakeGenerator.build("server_1", List.of("server_1", "server_2")))
                                .decoratorRule(StringDecoratorRule.builder().postfix("WWW").autoComplete(false).build())
                                .build()
                );
    }


    @Test
    public void test2() {
        String generatorId_1 = IDGenerator.nextToString("generatorId_1");
        assertThat(generatorId_1).startsWith("QQQ");

        String generatorId_2 = IDGenerator.nextToString("generatorId_2");
        assertThat(generatorId_2).endsWith("WWW");
    }

}

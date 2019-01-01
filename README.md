# Lian


## Profile

A simple and efficient ordered ID generator,
Can help you easily generate readable and beautiful business primary keys.<br>
Support Java8 and last
 

## maven

     <dependency>
       <groupId>ink.organics</groupId>
       <artifactId>lina</artifactId>
       <version>2.1</version>
     </dependency>
     
## A simple example

- Initialize some rule during program loading. like in spring

        @Bean
        public LinaConsole linaConsole() {
        
            return new LinaConsole().init(
                    new LinaConfig("TA", new TimeStampRule().prefix("TAA")),
                    new LinaConfig("TB", new TimeStampRule())
            ).boot(new TimeStampCodeDao());
            
        }

- You can get ID anywhere

        String id = LinaServer.nextCode("TA");
        


## Detail

### LinaConfig

LinaConfig 实例用于描述 "规则" 与 "规则key"的对应关系.类似于 Map<String,Rule> <br>
在获取ID时，需要明确规则的key。未被初始化的key是不容许的。

    String id = LinaServer.nextCode("Rule_Key");

       
### CodeDao

在部分情况下需要一个外部存储，在程序重启后Lina会尝试访问这些外部存储，以便获取程序终止前输出的最大有序ID，以保证程序重启后获取的ID不会重复<br>
因此，错误的CodeDao配置有可能导致组件无法正常工作或生成的有序ID重复<br>

CodeDao 有三种默认的实现，可以根据业务不同选择

- DefaultCodeDao<br>

默认的CodeDao实现，组件会将使用过的ID序列化到本地磁盘.
    
- RedisCodeDao<br>
    
组件会将使用过的ID序列化到Redis.

- TimeStampCodeDao<br>

组件不依赖外部存储，而依赖当前运行环境的系统时间(修改系统时间会生成重复的ID).
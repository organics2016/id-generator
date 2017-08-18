package net.vkits.platform.lina.service;


import net.vkits.platform.lina.dao.CodeDao;
import net.vkits.platform.lina.dao.impl.DefaultCodeDao;
import net.vkits.platform.lina.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Created by 王汗超 on 2017/4/6.
 */
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    private Producer() {
    }

    private static final Producer p = new Producer();

    public static Producer getInstance() {
        return p;
    }

    // Code一旦被消费，会进入这个队列，出队时根据上一个已被消费Code生成新的Code
    private final TransferQueue<Produce> produceQueue = new LinkedTransferQueue<>();

    // 生产规则
    private final RuleConfig ruleConfig = RuleConfig.getInstance();

    // 消费队列服务
    private final Consumer consumer = Consumer.getInstance();

    // 持久化层
    private CodeDao codeDao;

    public Producer init(CodeDao codeDao) {

        if (codeDao == null) {
            String classpath = this.getClass().getClassLoader().getResource("").getFile();
            this.codeDao = new DefaultCodeDao(classpath);
        } else
            this.codeDao = codeDao;

        return this;
    }

    public void start() {

        // 根据规则列表 初始化 每个规则的最大Code
        ruleConfig.getRuleMap().forEach((groupId, rule) -> {
            if (!codeDao.exists(groupId))
                codeDao.addCodeGroup(groupId);

            long maxCode = codeDao.getMaxCode(groupId);
            if (maxCode <= 0) {
                maxCode = rule.getStart();
            }

            this.produce(groupId, rule.format(maxCode));
        });

        this.startProduce();
    }

    private void startProduce() {
        new Thread(() -> {
            try {
                while (true) {

                    Produce produce = produceQueue.take();
                    String groupId = produce.getGroupId();
                    String maxCode = produce.getMaxCode();

                    Rule rule = this.ruleConfig.getRule(groupId);

                    maxCode = rule.produce(maxCode);

                    codeDao.updateMaxCode(groupId, rule.parse(maxCode));

                    consumer.put(groupId, maxCode);
                }
            } catch (InterruptedException e) {
                logger.error("error :", e);
            }
        }).start();
    }

    public void produce(String groupId, String maxCode) {
        try {
            produceQueue.put(new Produce(groupId, maxCode));
        } catch (InterruptedException e) {
            logger.error("error :", e);
        }
    }
}

class Produce {

    private final String groupId;

    private final String maxCode;

    public Produce(String groupId, String maxCode) {
        this.groupId = groupId;
        this.maxCode = maxCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getMaxCode() {
        return maxCode;
    }
}

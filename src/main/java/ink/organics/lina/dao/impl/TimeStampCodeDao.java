package ink.organics.lina.dao.impl;

import ink.organics.lina.dao.CodeDao;
import ink.organics.lina.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class TimeStampCodeDao implements CodeDao {

    private static final Logger log = LoggerFactory.getLogger(TimeStampCodeDao.class);

    private final Map<String, TransferQueue<Long>> codeQueue = new ConcurrentHashMap<>();

    private Map<String, Rule> ruleMap;

    @Override
    public void init(Map<String, Rule> ruleMap) {
        this.ruleMap = ruleMap;

        this.ruleMap.forEach((groupId, rule) -> {
            try {
                codeQueue.put(groupId, new LinkedTransferQueue<>());
                codeQueue.get(groupId).put(this.getCode(groupId));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public long getCode(String groupId) {
        return ruleMap.get(groupId).next(0);
    }

    @Override
    public long nextCode(String groupId) {
        try {
            long oldCode = codeQueue.get(groupId).take();

            long code = ruleMap.get(groupId).next(0);

            while (oldCode == code) {
                code = ruleMap.get(groupId).next(0);
            }
            codeQueue.get(groupId).put(code);
            return code;
        } catch (InterruptedException e) {
            log.error("error :", e);
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e1) {
                log.error("error :", e1);
            }
        }
        return ruleMap.get(groupId).next(0);
    }

    @Override
    public void addCodeGroup(String groupId, long initCode) {

    }

    @Override
    public boolean exists(String groupId) {
        return ruleMap.get(groupId) != null;
    }
}

package net.vkits.platform.lina.dao.impl;


import net.vkits.platform.lina.config.RuleConfig;
import net.vkits.platform.lina.dao.CodeDao;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Created by 王汗超 on 2017/4/5.
 */
public class DefaultCodeDao implements CodeDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCodeDao.class);

    private final Map<String, BufferedWriter> bwMap = new ConcurrentHashMap<>();

    private final Map<String, File> fileMap = new ConcurrentHashMap<>();

    private final RuleConfig ruleConfig = RuleConfig.getInstance();

    private final Map<String, TransferQueue<Long>> codeQueue = new ConcurrentHashMap<>();

    // Code一旦被消费，会进入这个队列，出队时根据上一个已被消费Code生成新的Code
    private final TransferQueue<Produce> produceQueue = new LinkedTransferQueue<>();

    private final File file;

    public DefaultCodeDao(String path) {
        this.file = new File(new File(path), "maxcode");
        this.init();
    }

    public DefaultCodeDao(File file) {
        this.file = new File(file, "maxcode");
        this.init();
    }


    private void init() {
        if (!this.file.isDirectory())
            this.file.mkdirs();

        ruleConfig.getRuleMap().forEach((groupId, rule) -> {
            try {
                codeQueue.put(groupId, new LinkedTransferQueue<>());

                File file = new File(this.file, groupId);
                BufferedWriter bw =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(file, true), "UTF-8"));
                this.bwMap.put(groupId, bw);
                this.fileMap.put(groupId, file);

                long maxCode = this.getCode(groupId);
                if (maxCode <= 0) {
                    maxCode = rule.getStart();
                } else {
                    maxCode = rule.next(maxCode);
                }

                codeQueue.get(groupId).put(maxCode);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        this.start();
    }


    @Override
    public long getCode(String groupId) {
        File file = this.fileMap.get(groupId);

        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, Charset.forName("UTF-8"))) {
            String maxCode = reader.readLine();
            return maxCode != null ? Long.valueOf(maxCode) : 0L;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long nextCode(String groupId) {
        try {
            long code = codeQueue.get(groupId).take();

            BufferedWriter bw = this.bwMap.get(groupId);
            bw.write(String.valueOf(code));
            bw.newLine();
            bw.flush();

            produceQueue.put(new Produce(groupId, code));
            return code;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void start() {

        new Thread(() -> {
            try {
                while (true) {

                    Produce produce = produceQueue.take();
                    String groupId = produce.getGroupId();
                    long code = produce.getCode();

                    codeQueue.get(groupId).put(this.ruleConfig.getRule(groupId).next(code));
                }
            } catch (InterruptedException e) {
                logger.error("error :", e);
            }
        }).start();
    }

    @Override
    public boolean exists(String groupId) {
        return this.fileMap.get(groupId).exists();
    }


    @Override
    public void addCodeGroup(String groupId, long initCode) {
        try {
            BufferedWriter bw = this.bwMap.get(groupId);
            bw.write(String.valueOf(initCode));
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Produce {

    private final String groupId;

    private final long code;

    public Produce(String groupId, long code) {
        this.groupId = groupId;
        this.code = code;
    }

    public String getGroupId() {
        return groupId;
    }

    public long getCode() {
        return code;
    }
}

package net.vkits.platform.lina.dao.impl;


import net.vkits.platform.lina.dao.CodeDao;
import net.vkits.platform.lina.rule.Rule;
import org.apache.commons.io.input.ReversedLinesFileReader;

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


    private final Map<String, BufferedWriter> bwMap = new ConcurrentHashMap<>();

    private final Map<String, File> fileMap = new ConcurrentHashMap<>();

    private final Map<String, TransferQueue<Long>> codeQueue = new ConcurrentHashMap<>();

    private final File file;

    private Map<String, Rule> ruleMap;

    public DefaultCodeDao(String path) {
        this(new File(path));
    }

    public DefaultCodeDao(File file) {
        this.file = new File(file, "maxcode");
    }

    @Override
    public void init(Map<String, Rule> ruleMap) {
        this.ruleMap = ruleMap;

        this.init();
    }

    private void init() {
        if (!this.file.isDirectory())
            this.file.mkdirs();

        this.ruleMap.forEach((groupId, rule) -> {
            try {
                codeQueue.put(groupId, new LinkedTransferQueue<>());

                File file = new File(this.file, groupId);
                BufferedWriter bw =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(file, true), "UTF-8"));
                this.bwMap.put(groupId, bw);
                this.fileMap.put(groupId, file);


                if (!this.exists(groupId)) {
                    this.addCodeGroup(groupId, rule.getStart());
                }

                long maxCode = rule.next(this.getCode(groupId));

                codeQueue.get(groupId).put(maxCode);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

            codeQueue.get(groupId).put(this.ruleMap.get(groupId).next(code));
            return code;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
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

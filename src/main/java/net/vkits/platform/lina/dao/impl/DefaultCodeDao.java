package net.vkits.platform.lina.dao.impl;


import net.vkits.platform.lina.dao.CodeDao;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 王汗超 on 2017/4/5.
 */
public class DefaultCodeDao implements CodeDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCodeDao.class);

    private final Map<String, BufferedWriter> bwMap = new ConcurrentHashMap<>();

    private final Map<String, File> fileMap = new ConcurrentHashMap<>();

    private final File file;

    public DefaultCodeDao(String path) {
        this.file = new File(new File(path), "maxcode");
        if (!file.isDirectory())
            file.mkdirs();
    }

    public DefaultCodeDao(File file) {
        this.file = new File(file, "maxcode");
        if (!file.isDirectory())
            file.mkdirs();
    }


    @Override
    public long getMaxCode(String groupId) {
        File file = this.fileMap.get(groupId);
        if (file == null)
            throw new RuntimeException();

        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, Charset.forName("UTF-8"))) {
            String maxCode = reader.readLine();
            return maxCode != null ? Long.valueOf(maxCode) : 0L;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMaxCode(String groupId, long maxCode) {
        BufferedWriter bw = this.bwMap.get(groupId);
        try {
            bw.write(String.valueOf(maxCode));
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            try {
                if (bw != null)
                    bw.close();
                logger.error("error :", e);
            } catch (IOException e1) {
                logger.error("error :", e);
            }
        }
    }

    @Override
    public void addCodeGroup(String groupId) {
        File file = new File(this.file, groupId);
        this.addCodeGroup(groupId, file);
    }

    @Override
    public boolean exists(String groupId) {
        return this.bwMap.containsKey(groupId);
    }


    private void addCodeGroup(String groupId, File file) {
        try {
            BufferedWriter bw =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(file, true), "UTF-8"));
            this.bwMap.put(groupId, bw);
            this.fileMap.put(groupId, file);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

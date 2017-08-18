package net.vkits.platform.lina.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Created by 王汗超 on 2017/4/1.
 */
public class Consumer {

    private Consumer() {
    }

    private static final Consumer q = new Consumer();

    public static Consumer getInstance() {
        return q;
    }

    private final Map<String, TransferQueue<String>> codeQueue = new ConcurrentHashMap<>();


    public void init(Collection<String> groupIds) {
        groupIds.forEach(groupId ->
                codeQueue.put(groupId, new LinkedTransferQueue<>()));
    }


    public void put(String groupId, String... codes) {
        try {
            TransferQueue<String> queue = codeQueue.get(groupId);
            for (String code : codes) {
                queue.put(code);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定ID分组的Code
     * 该方法为阻塞方法，即在获取到Code之前，该方法会一直阻塞。
     * 需要注意的是：当调用该方法的线程中断，该方法会快速返回null，而不是抛出RuntimeException
     *
     * @param groupId
     * @return
     */
    public String take(String groupId) {
        try {
            return codeQueue.get(groupId).take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * 获取指定ID分组的Code
     * 该方法为非阻塞方法，当指定分组的队列中没有Code时返回null
     *
     * @param groupId
     * @return
     */
    public String poll(String groupId) {
        return codeQueue.get(groupId).poll();
    }
}

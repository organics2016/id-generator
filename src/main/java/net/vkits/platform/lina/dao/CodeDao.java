package net.vkits.platform.lina.dao;

/**
 * Created by 王汗超 on 2017/4/5.
 * <p>
 * 编码持久化层
 */
public interface CodeDao {

    /**
     * 获取指定分组的最大Code值
     *
     * @param groupId
     * @return
     */
    long getMaxCode(String groupId);

    /**
     * 更新指定分组的最大Code值
     *
     * @param groupId
     * @param maxCode
     */
    void updateMaxCode(String groupId, long maxCode);

    /**
     * 添加一个分组
     *
     * @param groupId
     */
    void addCodeGroup(String groupId);

    /**
     * 判断指定分组是否存在
     *
     * @param groupId
     * @return
     */
    boolean exists(String groupId);
}

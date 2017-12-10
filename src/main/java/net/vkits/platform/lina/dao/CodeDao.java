package net.vkits.platform.lina.dao;

/**
 * Created by 王汗超 on 2017/4/5.
 * <p>
 * 编码持久化层
 */
public interface CodeDao {

    /**
     * 获取指定分组的Code值
     *
     * @param groupId
     * @return
     */
    long getCode(String groupId);

    /**
     * 更新指定分组的Code值
     *
     * @param groupId
     */
    long nextCode(String groupId);

    /**
     * 添加一个分组
     *
     * @param groupId
     * @param initCode
     */
    void addCodeGroup(String groupId, long initCode);

    /**
     * 判断指定分组是否存在
     *
     * @param groupId
     * @return
     */
    boolean exists(String groupId);
}

package net.vkits.platform.lina.rule;

/**
 * Created by 王汗超 on 2017/4/5.
 */
public interface Rule {

    /**
     * 根据当前规则生成下一个Code
     *
     * @param maxCode
     * @return ID集
     */
    String produce(String maxCode);


    /**
     * 根据当前规则生成下一个code
     *
     * @param maxCode
     * @return
     */
    long next(long maxCode);


    /**
     * 根据当前规则将 code 格式化为字符串
     *
     * @param code
     * @return
     */
    String format(long code);

    /**
     * 根据当前规则将字符串解析为 long
     *
     * @param code
     * @return
     */
    long parse(String code);

    /**
     * 返回该规则的起始值
     *
     * @return
     */
    long getStart();
}

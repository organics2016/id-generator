package net.vkits.platform.lina.rule.impl;


import net.vkits.platform.lina.rule.Rule;

/**
 * Created by 王汗超 on 2017/4/1.
 */
public class LinaRule implements Rule {


    /**
     * 前缀
     */
    private String prefix = "";

    /**
     * 后缀
     */
    private String postfix = "";

    /**
     * 步长
     */
    private int step = 1;

    /**
     * 是否自动补全
     */
    private boolean autoComple = true;

    /**
     * 最大位数
     */
    private int maxBit = 6;

    /**
     * 起始值
     */
    private long start = 0;


    @Override
    public long next(long maxCode) {
        return maxCode + (long) this.step;
    }

    @Override
    public String format(long code) {

        String strCode = String.valueOf(code);
        if (this.autoComple) {
            strCode = String.format("%0" + this.maxBit + "d", code);
        }

        if (strCode.length() > this.maxBit) {
            throw new RuntimeException("Have reached the maximum value");
        }

        return this.prefix + strCode + this.postfix;
    }

    @Override
    public long getStart() {
        return this.start;
    }

    public LinaRule prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public LinaRule postfix(String postfix) {
        this.postfix = postfix;
        return this;
    }

    public LinaRule step(int step) {
        this.step = step;
        return this;
    }

    public LinaRule autoComple(boolean autoComple) {
        this.autoComple = autoComple;
        return this;
    }

    public LinaRule maxBit(int maxBit) {
        this.maxBit = maxBit;
        return this;
    }

    public LinaRule start(long start) {
        this.start = start;
        return this;
    }
}



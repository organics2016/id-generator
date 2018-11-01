package net.vkits.platform.lina.rule.impl;

import net.vkits.platform.lina.rule.Rule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeStampRule implements Rule {

    /**
     * 前缀
     */
    private String prefix = "";

    /**
     * 后缀
     */
    private String postfix = "";

    /**
     * 日期格式
     */
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 时区
     */
    private ZoneId zoneId = ZoneId.systemDefault();

    /**
     * 起始时间戳
     */
    private long start = Instant.now().toEpochMilli();

    @Override
    public long next(long maxCode) {
        return Instant.now().toEpochMilli();
    }

    @Override
    public String format(long code) {
        return this.prefix + (Instant.ofEpochMilli(code).atZone(zoneId).format(formatter)) + this.postfix;
    }

    @Override
    public long getStart() {
        return this.start;
    }

    public TimeStampRule prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public TimeStampRule postfix(String postfix) {
        this.postfix = postfix;
        return this;
    }

    public TimeStampRule format(String format) {
        this.formatter = DateTimeFormatter.ofPattern(format);
        return this;
    }

    public TimeStampRule zone(String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
        return this;
    }

    public TimeStampRule start(long start) {
        this.start = start;
        return this;
    }
}

package net.vkits.platform.lina.rule.impl;

import net.vkits.platform.lina.rule.Rule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeStampRule implements Rule {


    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private ZoneId zoneId = ZoneId.systemDefault();

    private long start = Instant.now().toEpochMilli();

    @Override
    public String produce(String maxCode) {
        return LocalDateTime.now(zoneId).format(formatter);
    }

    @Override
    public long next(long maxCode) {
        return Instant.now().toEpochMilli();
    }

    @Override
    public String format(long code) {
        return Instant.ofEpochMilli(code).atZone(zoneId).format(formatter);
    }

    @Override
    public long parse(String code) {
        return LocalDateTime.parse(code, formatter).atZone(zoneId).toInstant().toEpochMilli();
    }

    @Override
    public long getStart() {
        return this.start;
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

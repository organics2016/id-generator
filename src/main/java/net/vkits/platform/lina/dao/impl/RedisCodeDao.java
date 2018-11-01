package net.vkits.platform.lina.dao.impl;

import net.vkits.platform.lina.dao.CodeDao;
import net.vkits.platform.lina.redis.Handler;
import net.vkits.platform.lina.redis.RedisHandler;
import net.vkits.platform.lina.rule.Rule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class RedisCodeDao implements RedisHandler, CodeDao {


    private final JedisPool jedisPool;

    public RedisCodeDao(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public <R> R redisHandler(Handler<Jedis, R> jedisHandler) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedisHandler.handler(jedis);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public void init(Map<String, Rule> ruleMap) {
        ruleMap.forEach((groupId, rule) -> {
            if (!this.exists(groupId)) {
                this.addCodeGroup(groupId, rule.getStart());
            }
        });
    }

    @Override
    public long getCode(String groupId) {
        return Long.valueOf(this.redisHandler(jedis -> jedis.get(groupId)));
    }

    @Override
    public long nextCode(String groupId) {
        return this.redisHandler(jedis -> jedis.incr(groupId));
    }

    @Override
    public void addCodeGroup(String groupId, long initCode) {
        this.redisHandler(jedis -> jedis.setnx(groupId, String.valueOf(initCode)));
    }

    @Override
    public boolean exists(String groupId) {
        return this.redisHandler(jedis -> jedis.exists(groupId));
    }
}

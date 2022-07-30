package ink.organics.lina.id.impl;

import ink.organics.lina.id.IDs;
import ink.organics.lina.redis.Handler;
import ink.organics.lina.redis.RedisHandler;
import ink.organics.lina.rule.Rule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class RedisIDs implements RedisHandler, IDs {


    private final JedisPool jedisPool;

    public RedisIDs(JedisPool jedisPool) {
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

    }

    @Override
    public long next(String groupId) {
        return this.redisHandler(jedis -> jedis.incr(groupId));
    }



}

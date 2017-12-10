package net.vkits.platform.lina.dao.impl;

import net.vkits.platform.lina.redis.Handler;
import net.vkits.platform.lina.redis.RedisHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao implements RedisHandler {


    private final JedisPool jedisPool;

    public RedisDao(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void redisHandler(Handler<Jedis> jedisHandler) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedisHandler.handler(jedis);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }
}

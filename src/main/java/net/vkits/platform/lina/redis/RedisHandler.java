package net.vkits.platform.lina.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by wanghc on 2016/8/18.
 */
public interface RedisHandler {

    void redisHandler(Handler<Jedis> jedisHandler);
}

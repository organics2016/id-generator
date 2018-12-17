package ink.organics.lina.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by wanghc on 2016/8/18.
 */
public interface RedisHandler {

    <R> R redisHandler(Handler<Jedis, R> jedisHandler);
}

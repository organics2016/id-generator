package ink.organics.idgenerator.generator.impl;

import ink.organics.idgenerator.generator.Generator;
import lombok.Builder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Builder
public class RedisGenerator implements Generator {

    public interface Handler<R> {
        R handler(Jedis event);
    }


    private final JedisPool jedisPool;

    private final String currentServiceId;


    public <R> R redisHandler(Handler<R> jedisHandler) {
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
    public long next() {
        return this.redisHandler(jedis -> jedis.incr(currentServiceId));
    }
}

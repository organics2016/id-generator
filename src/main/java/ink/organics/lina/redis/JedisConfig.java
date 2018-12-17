package ink.organics.lina.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisConfig {


    private final JedisPool jedisPool;

    public JedisConfig(String host, int port) {
        this.jedisPool = new JedisPool(this.getJedisPoolConfig(), host, port);
    }


    public JedisConfig(String host, int port, String password) {
        this.jedisPool = new JedisPool(this.getJedisPoolConfig(), host, port, 10000, password);
    }

    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
//        redis.setBlockWhenExhausted(true);
        //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
//        redis.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        //是否启用pool的jmx管理功能, 默认true
//        redis.setJmxEnabled(true);
        //是否启用后进先出, 默认true
//        redis.setLifo(true);
        //最大空闲连接数, 默认8个
//        redis.setMaxIdle(8);
        //最大连接数, 默认8个
//        redis.setMaxTotal(8);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
//        redis.setMaxWaitMillis(-1);
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
//        redis.setMinEvictableIdleTimeMillis(1800000);
        //最小空闲连接数, 默认0
//        redis.setMinIdle(0);
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
//        redis.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
//        redis.setSoftMinEvictableIdleTimeMillis(1800000);
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(true);
        //在空闲时检查有效性, 默认false
//        redis.setTestWhileIdle(false);
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
//        redis.setTimeBetweenEvictionRunsMillis(-1);
        return config;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}

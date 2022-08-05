package ink.organics.idgenerator.generator.impl;

import ink.organics.idgenerator.generator.Generator;
import ink.organics.idgenerator.utils.DigestUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SnowflakeGenerator implements Generator {

    private final long startTimestamp =
            LocalDate.of(2022, 1, 1)
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli();


    private final long sequenceBits = 12L;

    private final long instanceBits = 10L;

    private final long instanceMove = sequenceBits;

    private final long timestampMove = sequenceBits + instanceBits;

    private final long maxInstanceId = ~(-1L << instanceBits);

    private final long maxSequence = ~(-1L << sequenceBits);

    private final long instanceId;

    private final long getIdTimeout;

    private final AtomicLong lastTimestamp = new AtomicLong(-1);

    private final LinkedTransferQueue<Long> transferQueue = new LinkedTransferQueue<>();

    // ---

    private final String redisKeyPrefix = "service-";

    // TODO serverless 情况下怎么办
    private final long redisKeyKeepAliveTime = 10 * 60 * 1000;

    private final long redisKeyRefreshTime = 60 * 1000;

    private final JedisPool jedisPool;

    private final ScheduledExecutorService scheduledExecutorService;

    private SnowflakeGenerator(String currentServiceId, Collection<String> allServiceId, long getIdTimeout) {
        this.jedisPool = null;
        this.scheduledExecutorService = null;

        List<String> all = allServiceId.stream().distinct().toList();

        if (all.size() != allServiceId.size()) {
            throw new IllegalArgumentException("Services identifier list has repeating!");
        }

        if (all.size() >= maxInstanceId) {
            throw new IllegalArgumentException("Service identifier exceeds maximum!");
        }

        for (int i = 0; i < all.size(); i++) {
            if (currentServiceId.equals(all.get(i))) {
                this.instanceId = i;
                this.getIdTimeout = getIdTimeout;
                this.lastTimestamp.set(-1);
                return;
            }
        }

        throw new IllegalArgumentException("Not found " + currentServiceId + "in the services identifier list!");
    }


    private SnowflakeGenerator(String redisUrl, long getIdTimeout) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        this.jedisPool = new JedisPool(config, redisUrl);

        try (Jedis jedis = this.jedisPool.getResource()) {
            for (int i = 0; i < maxInstanceId; i++) {
                byte[] key = DigestUtils.sha256(redisKeyPrefix + i);

                String result = jedis.set(key, new byte[]{1},
                        SetParams.setParams().nx().px(redisKeyKeepAliveTime));

                if ("OK".equals(result)) {
                    this.instanceId = i;
                    this.getIdTimeout = getIdTimeout;
                    this.lastTimestamp.set(-1);
                    this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
                    this.scheduledExecutorService.scheduleWithFixedDelay(this::keepAlive, redisKeyRefreshTime, redisKeyRefreshTime, TimeUnit.MILLISECONDS);
                    return;
                }
            }
        }

        throw new RuntimeException("Service identifier has been exhausted waiting for Redis to be released!");
    }

    private void keepAlive() {
        try (Jedis jedis = this.jedisPool.getResource()) {
            byte[] key = DigestUtils.sha256(this.redisKeyPrefix + this.instanceId);
            String result = jedis.set(key, new byte[]{1},
                    SetParams.setParams().px(redisKeyKeepAliveTime));

            if (!"OK".equals(result)) {
                throw new RuntimeException("Service identifier refreshed fail.");
            }
        } catch (Exception e) {
            // 无论任何异常，都不能终止定时任务，尝试修复服务标识。
            log.error("Generator ID can't keep alive : ", e);
        }
    }

    private static final Map<String, SnowflakeGenerator> INSTANCE_MAP = new ConcurrentHashMap<>();

    private static Type INIT_TYPE;

    public static SnowflakeGenerator build(String currentServiceId, List<String> allServiceId) {
        return build(currentServiceId, allServiceId, 2000);
    }

    public static SnowflakeGenerator build(String redisUrl) {
        return build(redisUrl, 2000);
    }

    /**
     * 创建一个 SnowflakeGenerator 实例，如果相同 currentServiceId 再次调用则会返回第一次创建的实例
     * 这个实例 是以 currentServiceId 为单例
     *
     * @param currentServiceId Current service id
     * @param allServiceId     All service id
     * @param getIdTimeout     get id timeout  Unit: ms
     * @return Return a SnowflakeGenerator instance
     */
    public static SnowflakeGenerator build(String currentServiceId, List<String> allServiceId, long getIdTimeout) {
        if (INIT_TYPE == null) {
            INIT_TYPE = Type.DEFAULT;
        } else if (!INIT_TYPE.equals(Type.DEFAULT)) {
            throw new IllegalArgumentException(SnowflakeGenerator.class.getName() + " has been initialized with " + INIT_TYPE);
        }
        return INSTANCE_MAP.computeIfAbsent(currentServiceId, (key) -> new SnowflakeGenerator(key, allServiceId, getIdTimeout));
    }

    public static SnowflakeGenerator build(String redisUrl, long getIdTimeout) {
        if (INIT_TYPE == null) {
            INIT_TYPE = Type.REDIS;
        } else if (!INIT_TYPE.equals(Type.REDIS)) {
            throw new IllegalArgumentException(SnowflakeGenerator.class.getName() + " has been initialized with " + INIT_TYPE);
        }
        return INSTANCE_MAP.computeIfAbsent(redisUrl, (key) -> new SnowflakeGenerator(redisUrl, getIdTimeout));
    }


    private void generate(final long num) {
        this.lastTimestamp.updateAndGet((lastTimestamp -> {

            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp <= lastTimestamp) {

                long diff = lastTimestamp - currentTimestamp;

                if (diff > 1000) {
                    log.error("System time may be wrong. The current timestamp is {}, id generate last timestamp is {} with {} ms difference",
                            currentTimestamp,
                            lastTimestamp,
                            diff);

                    return lastTimestamp;
                }
                currentTimestamp = lastTimestamp + 1;
            }

            for (long s = 0; s < num && s < maxSequence; s++) {
                long id = ((currentTimestamp - startTimestamp) << timestampMove) |
                        (instanceId << instanceMove) |
                        s;

                transferQueue.put(id);
            }

            return currentTimestamp;
        }));
    }

    @Override
    public long next() {
        try {

            Long id = transferQueue.poll();
            if (id == null) {
                synchronized (this) {
                    // 上锁之后再次尝试获取，其他线程可能已生产完成
                    id = transferQueue.poll();
                    if (id == null) {
                        generate(maxSequence);
                        id = transferQueue.poll(getIdTimeout, TimeUnit.MILLISECONDS);
                        if (id == null) {
                            // 没有ID应该超时 使上层事务结束
                            throw new RuntimeException("Generate id timeout!");
                        }
                    }
                }
            }

            return id;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private enum Type {
        DEFAULT,
        REDIS
    }
}
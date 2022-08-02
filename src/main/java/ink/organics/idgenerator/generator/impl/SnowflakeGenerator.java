package ink.organics.idgenerator.generator.impl;

import ink.organics.idgenerator.generator.Generator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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

    private final AtomicLong lastTimestamp = new AtomicLong(0);

    private final LinkedTransferQueue<Long> transferQueue = new LinkedTransferQueue<>();

    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


    private SnowflakeGenerator(String currentServiceId, Collection<String> allInstance, long getIdTimeout) {
        List<String> all = allInstance.stream().distinct().sorted().toList();

        if (all.size() != allInstance.size()) {
            throw new IllegalArgumentException("Services identifier list has repeating!");
        }

        if (all.size() >= maxInstanceId) {
            throw new IllegalArgumentException("Service identifier exceeds maximum!");
        }

        for (int i = 0; i < all.size(); i++) {
            if (currentServiceId.equals(all.get(i))) {
                this.instanceId = i;
                this.getIdTimeout = getIdTimeout;
                lastTimestamp.set(-1);
                return;
            }
        }

        throw new IllegalArgumentException("Not found " + currentServiceId + "in the services identifier list!");
    }

    private static final Map<String, SnowflakeGenerator> INSTANCE_MAP = new ConcurrentHashMap<>();

    public static SnowflakeGenerator build(String currentServiceId, List<String> allInstance) {
        return INSTANCE_MAP.computeIfAbsent(currentServiceId, (key) -> new SnowflakeGenerator(key, allInstance, 2000));
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
        return INSTANCE_MAP.computeIfAbsent(currentServiceId, (key) -> new SnowflakeGenerator(key, allServiceId, getIdTimeout));
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
                        singleThreadExecutor.submit(() -> generate(maxSequence));
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
}

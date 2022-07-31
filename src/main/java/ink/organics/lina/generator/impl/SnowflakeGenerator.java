package ink.organics.lina.generator.impl;

import ink.organics.lina.generator.Generator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
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

    private final AtomicLong lastTimestamp = new AtomicLong(0);
    private final long instanceId;

    private final LinkedTransferQueue<Long> transferQueue = new LinkedTransferQueue<>();

    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


    private SnowflakeGenerator(String currentInstance, Collection<String> allInstance) {
        List<String> all = allInstance.stream().distinct().sorted().toList();

        if (all.size() != allInstance.size()) {
            throw new IllegalArgumentException("Data center id list has repeating!");
        }

        if (all.size() >= maxInstanceId) {
            throw new IllegalArgumentException("Data center exceeds maximum!");
        }

        for (int i = 0; i < all.size(); i++) {
            if (currentInstance.equals(all.get(i))) {
                this.instanceId = i;
                lastTimestamp.set(-1);
                return;
            }
        }

        throw new IllegalArgumentException("Not found " + currentInstance + "in the Data center");
    }

    private static final Map<String, SnowflakeGenerator> INSTANCE_MAP = new ConcurrentHashMap<>();

    public static SnowflakeGenerator build(String currentInstance, Collection<String> allInstance) {
        return INSTANCE_MAP.computeIfAbsent(currentInstance, (key) -> new SnowflakeGenerator(key, allInstance));
    }

    private void generate(final int num) {
        this.lastTimestamp.updateAndGet((lastTimestamp -> {

            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp <= lastTimestamp) {
                log.warn("System time may be wrong. The current timestamp is {}, id generate last timestamp is {} with {} ms difference",
                        currentTimestamp,
                        lastTimestamp,
                        lastTimestamp - currentTimestamp);

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
                    id = transferQueue.poll();
                    if (id == null) {
                        singleThreadExecutor.submit(() -> generate(100));
                        id = transferQueue.take();
                    }
                }

            }

            return id;

        } catch (InterruptedException e) {
            singleThreadExecutor.shutdown();
            throw new RuntimeException(e);
        }
    }
}

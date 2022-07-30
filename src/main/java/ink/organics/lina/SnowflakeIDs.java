package ink.organics.lina;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class SnowflakeIDs {


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


    public SnowflakeIDs(String currentInstance, Collection<String> allInstance) {
        List<String> all = allInstance.stream().distinct().sorted().toList();

        if (all.size() != allInstance.size()) {
            throw new IllegalArgumentException("ddd");
        }

        if (all.size() >= maxInstanceId) {
            throw new IllegalArgumentException("ddd");
        }

        for (int i = 0; i < all.size(); i++) {
            if (currentInstance.equals(all.get(i))) {
                this.instanceId = i;
                lastTimestamp.set(-1);
                return;
            }
        }

        throw new IllegalArgumentException("ddd");
    }

    private void generate() {
        this.lastTimestamp.updateAndGet((lastTimestamp -> {

            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp <= lastTimestamp) {
                currentTimestamp = lastTimestamp + 1;
            }

            long maxFutureTimestamp = currentTimestamp + 2;
            for (long futureTimestamp = currentTimestamp; futureTimestamp < maxFutureTimestamp; futureTimestamp++) {

                for (long s = 0; s < maxSequence; s++) {
                    long id = ((futureTimestamp - startTimestamp) << timestampMove) |
                            (instanceId << instanceMove) |
                            s;

                    transferQueue.put(id);
                }
            }

            return maxFutureTimestamp;
        }));
    }

    public long next() {
        try {

            Long id = transferQueue.poll();
            if (id == null) {

                synchronized (this) {
                    id = transferQueue.poll();
                    if (id == null) {
                        singleThreadExecutor.submit(this::generate);
                        id = transferQueue.take();
                    }
                }

            }

            return id;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

//        System.out.println(Long.toBinaryString(-1));
//        System.out.println(Long.parseLong("0111111111111111111111111111111111111111111111111111111111111111", 2));
//        System.out.println(Long.parseLong("011111111111111111111111111111111111111111", 2));
//        System.out.println(Long.parseLong("01111111111", 2));
//        System.out.println(Long.parseLong("0111111111111", 2));

        SnowflakeIDs snowflakes = new SnowflakeIDs("s6", List.of("s1", "s2", "s3", "s4", "s5", "s6"));

        final ExecutorService executor = Executors.newFixedThreadPool(12);

        Map<Long, Object> map = new ConcurrentHashMap<>();

        for (int t = 0; t < 1000; t++) {

            executor.submit(() -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < 10000; i++) {
                    map.put(snowflakes.next(), "");
                }
                long end = System.currentTimeMillis();
                System.out.println(Thread.currentThread().getId() + " : " + (end - start));
            });

        }

        try {
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdown();
                System.out.println(map.size() == (1000 * 10000));

                long count = 0;
                while (snowflakes.transferQueue.poll() != null) {
                    count++;
                }
                System.out.println(count);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}

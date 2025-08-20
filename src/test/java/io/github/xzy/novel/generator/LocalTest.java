package io.github.xzy.novel.generator;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LocalTest {
    // 实现一个Redis线程池
    private static final int WORKER_ID = 1;           // 可从配置或注册中心获取
    private static final int SEQUENCE_BITS = 12;      // 序列号占 12 位，支持 4095 个/毫秒
    private static final int WORKER_ID_BITS = 5;      // 机器 ID 占 5 位
    private static final int TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS); // 4095

    // ==================== Redisson 客户端（替代 JedisPool）====================
    private static final RedissonClient redissonClient = createRedissonClient();
    private static final String REDIS_SEQ_KEY = "";

    private static RedissonClient createRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")  // Redis 地址
                .setConnectionPoolSize(16)
                .setConnectionMinimumIdleSize(4);

        return Redisson.create(config);
    }

    private Long getId(String prefix){
        // 1.读取时间戳
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.toInstant(ZoneOffset.UTC).toEpochMilli();

        // 2. 获取 workerId
        long workerId = Thread.currentThread().threadId() & 32;

        // 3. 从 Redis 获取序列号（每毫秒从 0 开始，但 Redis 自增是全局）
        //    我们用 prefix + timestamp 作为 key，实现“每毫秒重置”的效果
        String redisKey = REDIS_SEQ_KEY + prefix + ":" + timestamp;
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);

            // 原子自增
            long sequence = atomicLong.incrementAndGet();
            // 如果是第一次创建，设置过期时间（防止 key 堆积）
            if (sequence == 1) {
                atomicLong.expire(Instant.now().minus(-2, ChronoUnit.SECONDS));
            }

            // 控制序列号范围
            if (sequence > MAX_SEQUENCE) {
                throw new RuntimeException("Sequence overflow for key: " + redisKey);
            }

            // 4. 拼接 ID：(timestamp << 17) | (workerId << 12) | sequence
            return (timestamp << TIMESTAMP_LEFT_SHIFT) |
                    ((long) workerId << SEQUENCE_BITS) |
                    sequence;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ID", e);
        }

    }

//    @Test
    void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(100);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                Long id = getId("test");
                System.out.printf("id: %s%n", id);
                redissonClient.getBucket(String.valueOf(id)).set("0");

            }
            latch.countDown();
        };
        ExecutorService executor = Executors.newFixedThreadPool(100);
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            executor.submit(task);
        }
        latch.await();
        long time = System.currentTimeMillis() - currentTimeMillis;
        System.out.println(time+ " ms");

    }
}

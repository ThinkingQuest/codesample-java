package net.thinkingquest.ratelimit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 以分钟为单位的限流器的一个简单实现。
 * Guava库中的RateLimiter是以秒为单位，很难用于以分钟为单位。
 * 没有阻塞线程方法的实现。 如果未来需要，可以增加acquire(int maxWaitTime) 方法，当没有令牌时阻塞线程等待。
 *
 * 实现策略：记录下一个可以获得令牌的时间的毫秒数nextFreeTicketMills。
 * 当获取令牌时，对比当前时间戳和nextFreeTicketMills，决定是否可以获得令牌。
 * 获得令牌后，将nextFreeTicketMills增加。
 * 将nextFreeTicketMills可以是一个过去的时间，但不应小于当前时间戳减去一个特定的值，否则就拥有了过多令牌。
 * 这个特定的值，由"允许累积的最长时间"决定。
 */
public class MinuteRateLimiter {

    /**
     * 平均多少毫秒新增1个令牌
     */
    private double millsPerPermit;

    /**
     * 未使用的令牌，最大可以累积多长时间
     */
    private double maxReserveMills;

    /**
     * 下一个可以拿到令牌的毫秒时间戳， 可能是未来时间，也可能是过去时间。
     */
    private double nextFreeTicketMills;

    private volatile Object mutex = new Object();

    /**
     * 示例： new MinuteRateLimiter(120, 2) 创造的rateLimiter，
     * 每分钟有120个令牌，最大可以累积2分钟，即长时间未取令牌的情况下，最多可以积累240个令牌。
     * 没有令牌积累的情况下，平滑每秒可以取得2个令牌。
     *
     * @param permitsPerMinute 每分钟允许的次数
     * @param maxBurstMinutes 最大允许累计的分钟数
     */
    public MinuteRateLimiter(int permitsPerMinute, int maxBurstMinutes) {
        this.millsPerPermit = 60000 / ((double) permitsPerMinute);
        this.maxReserveMills = maxBurstMinutes * 60000;
        this.nextFreeTicketMills = System.currentTimeMillis() - maxReserveMills;
    }

    public boolean tryAcquire() {
        synchronized (mutex) {
            long now = System.currentTimeMillis();
            if (nextFreeTicketMills <= now) {
                nextFreeTicketMills = nextFreeTicketMills + millsPerPermit;
                nextFreeTicketMills = Math.max(nextFreeTicketMills, now - maxReserveMills);
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final MinuteRateLimiter rateLimiter = new MinuteRateLimiter(20, 1);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 200; i++) {
            final int j = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (rateLimiter.tryAcquire()) {
                        System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": do job " + j + ".");
                    } else {
                        System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": blocked " + j + ".");
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Thread.sleep(3000 * 1000L);
    }
}

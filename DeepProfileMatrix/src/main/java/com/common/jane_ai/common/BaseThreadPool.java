package com.common.jane_ai.common;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BaseThreadPool {
    
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(

                Runtime.getRuntime().availableProcessors() + 1,
                maximumPoolSize(),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(300),
                new ThreadFactory() {
                    private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                    private AtomicInteger count = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = defaultFactory.newThread(r);
                        //内部cas操作保证原子性
                        thread.setName("BaseThreadPool-" + count.getAndIncrement());
                        return thread;
                    }
                }, // 线程名前缀，方便排查日志
                new ThreadPoolExecutor.AbortPolicy()
    );

    public ThreadPoolExecutor getExecutor() {
        return threadPoolExecutor;
    }


    //执行提交的任务
    public void execute(Runnable r) {
        threadPoolExecutor.execute(r);
    }

    //执行带返回值的任务
    public  <T> Future<T> submit(Callable<T> c) {
        return threadPoolExecutor.submit(c);
    }

    private static Integer maximumPoolSize() {
        return new BigDecimal(Runtime.getRuntime().availableProcessors())
                .divide(new BigDecimal("0.2"), 0, RoundingMode.HALF_UP).intValue();
    }
}

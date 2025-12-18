package com.common.jane_ai.common;

import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

//限流
@Component
@Slf4j
public class RateLimiter {
    private final Semaphore semaphore;
    private final TimeUnit timeUnit;
    public static final int DEFAULT_PERMITS_PER_SECOND = 200;

    //- 并发限制不等于 QPS：处理时间长但并发低时，QPS 可能很低；反之处理很快时，QPS 可能很高
    //- QPS 限制不等于并发安全：即使每秒只放行 200 个，如果每个请求持续 10 秒，系统很快就会积压到 2000 个并发
    /**
     * 默认构造函数200个
     */
    public RateLimiter() {
        this.semaphore=new Semaphore(DEFAULT_PERMITS_PER_SECOND);
        this.timeUnit=TimeUnit.SECONDS;
    }
    /**
     * 每秒允许的数量
     * @param permitsPerSecond
     */
    public RateLimiter(int permitsPerSecond) {
        this.semaphore = new Semaphore(permitsPerSecond);
        this.timeUnit = TimeUnit.SECONDS;
    }

    public boolean acquire() throws InterruptedException {
        if( !semaphore.tryAcquire(1, timeUnit) ){
//            throw new JaneFrameworkException(BaseCode.RATE_LIMITER_EXCEPTION);
            return false;
        }
        return true;
    }

    public void release() {
        semaphore.release();
    }
}

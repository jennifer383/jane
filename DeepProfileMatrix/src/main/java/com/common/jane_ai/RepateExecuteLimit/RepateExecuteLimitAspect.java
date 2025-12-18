package com.common.jane_ai.RepateExecuteLimit;
import com.common.jane_ai.caffine.LocallocakCache;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.*;
import static com.common.jane_ai.constant.JaneTypeConstant.PDF_MIDENG;
import static com.common.jane_ai.constant.JaneTypeConstant.REGISTER_MIDENG;

@Aspect
@Slf4j
@Component
//幂等：会话 注册
public class RepateExecuteLimitAspect {
    @Autowired
    private LocallocakCache locallocakCache;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //获取方法参数的名称列表
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    //解析 SpEL 表达式
    private final ExpressionParser parser = new SpelExpressionParser();
    private static final String MIDENG_KEY = "mideng";

    // 定义切入点，匹配所有被 @RepateExecuteLimit 注解的方法
    //切入点定义，用来匹配你想拦截的方法。
    @Pointcut("@annotation(com.common.jane_ai.RepateExecuteLimit.RepateExecuteLimit)")
    public void repateExecuteLimitpoint() {
        //这个方法本身不会执行任何代码，它只是切入点的命名容器。
    }
    //repateExecuteLimitpoint() 引用你定义的切入点；
//    @annotation(repateExecuteLimit) 表示将目标方法上注解对象注入到参数 repateExecuteLimit。
    @Around("repateExecuteLimitpoint() && @annotation(repateExecuteLimit)")
    public Object around(ProceedingJoinPoint joinPoint,RepateExecuteLimit repateExecuteLimit) throws Throwable {
        //幂等持续时间
        long durationTime = repateExecuteLimit.durationTime();
        //不需要userid，用户与游客都是5s
        String localName = "";
        if(repateExecuteLimit.type().equals(REGISTER_MIDENG)){
            //SpEL取值
            String userName = getBYSpEL(joinPoint, repateExecuteLimit);
            localName = JANE_REGISTER_GAP_TIME + userName;
        }else if(repateExecuteLimit.type().equals(JaneTypeConstant.CHAT_MIDENG)){
            String chatId = getBYSpEL(joinPoint, repateExecuteLimit);
            localName = JANE_CHAT_GAP_TIME + chatId;
        }else if(repateExecuteLimit.type().equals(PDF_MIDENG)){
            String userId = getBYSpEL(joinPoint, repateExecuteLimit);
            String userName = redisTemplate.opsForValue().get(JANE_USER_HAS_LOGIN + userId);
            localName = JANE_PDF_GAP_TIME + userName;
        }
        log.info(localName);
        String localValue = (String)redissonClient.getBucket(localName).get();

        if(Objects.equals(MIDENG_KEY, localValue)) {//幂等组件生效
            if(repateExecuteLimit.type().equals(PDF_MIDENG)) {
                throw new JaneFrameworkException(BaseCode.MIDENG_PDF_HAS_EXIST_EXCEPTION);
            } else {
                throw new JaneFrameworkException(BaseCode.MIDENG_HAS_EXIST_EXCEPTION);
            }
        }
        //无幂等标识
        ReentrantLock lock = locallocakCache.getLock(localName);
        boolean tryLock = lock.tryLock(1, TimeUnit.SECONDS);
        if(!tryLock){
            throw new JaneFrameworkException(BaseCode.MIDENG_HAS_EXIST_EXCEPTION);
        }
        try {
            String get2value=(String)redissonClient.getBucket(localName).get();
            if(MIDENG_KEY.equals(get2value)){
                throw new JaneFrameworkException(BaseCode.MIDENG_HAS_EXIST_EXCEPTION);
            }
            Object object = joinPoint.proceed();
            //执行完之后再加标识
            try {
                redissonClient.getBucket(localName).set(MIDENG_KEY,durationTime,TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("redisson-set-error");
                throw new JaneFrameworkException(BaseCode.MIDENG_NO_AVALIABLE_EXCEPTION);
            }
            return object;
        } finally {
            lock.unlock();
        }
        //我的单体项目的幂等只需要本地锁+caffine而不是redis
    }

    //写法	是否安全	Null 会怎么样
    //(String) get()	安全	返回 null
    //get().toString()	不安全 ❌	抛 NullPointerException

    //joinpoint代表的是 被 AOP 拦截的方法调用（连接点），里面包含了：
    //被代理的目标对象（Target Object）
    //被调用的方法（Method）
    //方法参数（Args）
    //方法签名（Signature）
    /**
     * 通过 SpEL（Spring Expression Language）表达式动态获取方法参数中的某个值。
     * @param joinPoint
     * @param repateExecuteLimit
     * @return
     */
    private String getBYSpEL(ProceedingJoinPoint joinPoint, RepateExecuteLimit repateExecuteLimit) {
        // 获取方法签名  方法签名由方法名 + 参数类型列表组成：
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 通过签名拿到被拦截方法的 Method 对象（反射）
        Method method = signature.getMethod();
        String spel = "";
        if(repateExecuteLimit.type().equals(REGISTER_MIDENG)){
            spel = repateExecuteLimit.userName();
        }else if(repateExecuteLimit.type().equals(JaneTypeConstant.CHAT_MIDENG)){
            spel = repateExecuteLimit.chatId();
        }else if(repateExecuteLimit.type().equals(PDF_MIDENG)){
            spel = repateExecuteLimit.userId();
        }
        // 获取切入方法的所有参数值
        Object[] args = joinPoint.getArgs();
        //spEl的构建
        //构建一个 SpEL 的上下文环境（用于让 SpEL 能解析参数名对应的值）
        EvaluationContext context = new MethodBasedEvaluationContext(
                null,        // rootObject 可传 null，除非你要引用 this
                method,      // 当前执行的方法
                args,        // 参数的实际值
                nameDiscoverer // 用于根据反射拿到参数名（SpEL 需要这个）
        );
        //解析参数名来替换成真正的参数值
        Object objKey = parser.parseExpression(spel).getValue(context);
        // 安全地转换为字符串返回（防止 null）
        return ObjectUtils.nullSafeToString(objKey);
    }
}

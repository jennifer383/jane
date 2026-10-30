package com.common.jane_ai.filter;

import com.common.jane_ai.common.BCryptPasswordUtils;
import com.common.jane_ai.common.RateLimiter;
import com.common.jane_ai.common.ResponseUtils;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
//options消息不能直接返回必须走完过滤器，否则第二个请求获取不到或者不能访问请求头
//必须完整的返回 否则不会正确发送请求
import static com.common.jane_ai.constant.JaneTypeConstant.AUTHORIZATION;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

//请求过滤器
//@WebFilter("/*")
// WebFilter("/*")表示对所有请求进行过滤 springboot方式过滤

//在很多 Java Web 服务器,处理请求的是线程池里的线程。
//所以请求处理完了，线程并不会终止，而是被放回线程池，等待下一个请求使用,手动清理threadlocal。
@Slf4j
public class RequestValidationFilter implements Filter {
//但是 @WebFilter 是 Java Servlet 原生注解，由容器（如 Tomcat）管理，不是由 Spring 管理
    //不能使用@Autoiwired
    //使用setter注入
    private List<RequestValidationStrategy> listOfStrategy;

    private RateLimiter rateLimiter;

    private BCryptPasswordUtils bCryptPasswordUtils;
    //没有token不允许访问的资源路径

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        boolean acquired = false; // 标记是否成功获取到许可
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            //限流semaphero
            acquired = rateLimiter.acquire();
            if (!acquired) {
                log.info("限流失败，请求被拒绝");
                ResponseUtils.writeError(response, BaseCode.RATE_LIMITER_EXCEPTION);
                return;
            }
            log.info("限流成功");

            if (request.getMethod().equals("OPTIONS")) {
                //预检请求
                //预检请求不只是需要返回 200，还必须附带完整的 CORS 响应头
                //✅这是浏览器的安全策略决定的特性，与 fetch、axios 无关 ——
                // 无论你用哪种请求库，只要发生了跨域并包含了“复杂请求”条件，浏览器就一定会自动发起 预检请求（OPTIONS）。
                log.info("OPTIONS请求 {}", request.getRequestURI());
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setStatus(HttpServletResponse.SC_OK);

                return;
            }

//          token的解析验证
            boolean beforeFilter = beforeFilter(request, response, filterChain);
            if(!beforeFilter) {
//                ResponseUtils.writeError(response,BaseCode.RATE_LIMITER_EXCEPTION);
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);//过滤
            afterFilter(request,response,filterChain);
            log.info("用户请求结束");
        } catch (InterruptedException e) {
            log.info("当前用户过多，请稍后再试");
            throw new RuntimeException(e);
        }finally{
            log.info("REQUEST_FILTER-"+Thread.currentThread().getName());
//            /手动清理数据
            BaseParameterHolder.removeThreadLocalMap();
            if (acquired) {
                rateLimiter.release();
            }
        }

    }

    /**
     * 请求执行前的逻辑
     * @param request
     * @param response
     * @param filterChain
     *///游客就是userid=0
    private boolean beforeFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        // 从请求头获取token和userId
        String token = request.getHeader(AUTHORIZATION);
        String userId = request.getHeader(USER_ID);
        
        // 如果请求头中没有userId，尝试从URL参数获取
        if (userId == null || userId.isEmpty()) {
            userId = request.getParameter(USER_ID);
        }
        
        // 如果URL参数中也没有userId，设置默认值为0（游客）
        if (userId == null || userId.isEmpty()) {
            userId = "0";
        }
        
        // 将token和userId存储到ThreadLocal中
        BaseParameterHolder.setBaseParameter(USER_ID, userId);
        BaseParameterHolder.setBaseParameter(AUTHORIZATION, token);
        
        log.info("用户登录-id：{}", BaseParameterHolder.getBaseParameter(USER_ID));
        // 仅记录认证凭据是否存在，避免插件登录后的 JWT 被写入日志。
        log.debug("用户请求携带认证凭据：{}", token != null && !token.isBlank());
        
        String path = request.getRequestURI();//请求路径
        log.info("用户请求开始-path-{}", path);
        
        // 遍历所有策略，找到匹配的策略并执行
        for(RequestValidationStrategy strategy : listOfStrategy) {
            if(strategy.matching(path)) {
                return strategy.execute(request, response);
                 // 响应已经写出，停止后续执行
            }
        }
        
        // 没有匹配的策略就直接结束
        ResponseUtils.writeError(response, BaseCode.REQUEST_UNUSABLE_EXCEPTION);
        return false;
        //如果为游客，没有token,会话接口 可以使用，只有当前的会话，前端拦截创建新会话（，创建一个游客会话以及游客标识,前端只能查询当前会话历史
        //过期时间为2h

        //如果为用户，没有token,如果是登录，就通过，否则就提示让登录

        //用户+token-> 在会话加入用户标识
    }

    private void afterFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    }

//1、init() 方法  初始化过滤器配置  有默认的init方法，可以不重写
//2、doFilter() 方法 过滤器的具体行为，abstract 方法，需要自己实现
//3、destroy() 方法 销毁过滤器  有默认的destroy方法，可以不重写

    public void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public void setBCryptPasswordUtils(BCryptPasswordUtils bCryptPasswordUtils) {
        this.bCryptPasswordUtils = bCryptPasswordUtils;
    }

    public void setRequestValidationStrategy(List<RequestValidationStrategy> requestValidationStrategy) {
        this.listOfStrategy= requestValidationStrategy;
    }
}
//

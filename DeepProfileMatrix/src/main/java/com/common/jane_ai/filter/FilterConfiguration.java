package com.common.jane_ai.filter;

import com.common.jane_ai.common.BCryptPasswordUtils;
import com.common.jane_ai.common.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class FilterConfiguration {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private BCryptPasswordUtils bCryptPasswordUtils;

    @Autowired//Spring会自动扫描Strategy的所有实现类,并注入到 List<ValidationStrategy>
    private List<RequestValidationStrategy> requestValidationStrategy;
    //：为了让非Spring Bean的过滤器能够使用Spring容器中的组件，必须通过FilterRegistrationBean进行注册。
    //在 Spring Boot 中，如果你要配置 多个过滤器，可以通过注册多个
    // FilterRegistrationBean 来实现，并通过 setOrder(int) 方法来控制它们的执行顺序（数字越小越先执行）。
    @Bean
    public FilterRegistrationBean<RequestValidationFilter> getFilter() {
        FilterRegistrationBean<RequestValidationFilter> filterBean = new FilterRegistrationBean();
        RequestValidationFilter filter = new RequestValidationFilter();
        filter.setRateLimiter(rateLimiter);
        filter.setBCryptPasswordUtils(bCryptPasswordUtils);
        filter.setRequestValidationStrategy(requestValidationStrategy);
        filterBean.setFilter(filter);
        filterBean.addUrlPatterns("/*");
        filterBean.setOrder(1); // 控制过滤器的执行顺序（越小越早）
        return filterBean;
    }
}

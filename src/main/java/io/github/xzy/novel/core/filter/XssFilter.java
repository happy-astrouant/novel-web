package io.github.xzy.novel.core.filter;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.github.xzy.novel.core.config.XssProperties;
import io.github.xzy.novel.core.wrapper.XssHttpServletRequestWrapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 防止 XSS 攻击的过滤器
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
@Component
@ConditionalOnProperty(value = "novel.xss.enabled", havingValue = "true")
@WebFilter(urlPatterns = "/*", filterName = "xssFilter")
@EnableConfigurationProperties(value = {XssProperties.class})
@RequiredArgsConstructor
public class XssFilter implements Filter {

    private final XssProperties xssProperties;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        // 如果是被排除的URL，不进行XSS检查，直接放行
        if (handleExcludeUrl(req)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 进行包装（净化），将<和>转换为标签实体，破坏恶意的标签机构
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
            (HttpServletRequest) servletRequest);
        // 净化完成后再放行
        filterChain.doFilter(xssRequest, servletResponse);
    }

    /*
    * 检查该请求是否是XSS配置被排除的URL
    * */
    private boolean handleExcludeUrl(HttpServletRequest request) {
        if (CollectionUtils.isEmpty(xssProperties.excludes())) {
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : xssProperties.excludes()) {
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
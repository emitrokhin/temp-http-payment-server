package ru.emitrohin.paymentserver;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

@WebFilter("/*")  // Применение ко всем страницам
public class EnvironmentFilter implements Filter {

    private String environment;

    @Override
    public void init(FilterConfig filterConfig) {
        environment = System.getenv("environment");
        System.out.println("Environment variable: " + environment);
        if (environment == null) {
            environment = "prod";
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setAttribute("environment", environment);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
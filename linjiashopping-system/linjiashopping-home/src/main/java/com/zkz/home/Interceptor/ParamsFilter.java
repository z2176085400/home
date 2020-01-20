package com.zkz.home.Interceptor;


import com.zkz.home.config.ParameterRequestWrapper;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Component
@WebFilter(urlPatterns = "/**", filterName = "ParamsFilter", dispatcherTypes = DispatcherType.REQUEST)
public class ParamsFilter implements Filter {


    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
            throws IOException, ServletException {
        ParameterRequestWrapper parmsRequest = new ParameterRequestWrapper((HttpServletRequest) arg0);
        arg2.doFilter(parmsRequest, arg1);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }


}

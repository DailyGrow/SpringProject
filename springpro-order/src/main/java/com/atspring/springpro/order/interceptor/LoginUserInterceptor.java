package com.atspring.springpro.order.interceptor;

import com.atspring.common.constant.AuthServerConstant;
import com.atspring.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static  ThreadLocal<MemberRespVo> LoginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        ///order/order/status/** 对于该路径放行（这是远程调用，无需用户登录）
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**",uri);
        if(match){
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute!=null){

            LoginUser.set(attribute);
            return true;
        }else{
            //没登陆就去登录 TODO complete login
            request.getSession().setAttribute("msg","请先进行登录");
            response.sendRedirect("http://auth.springpro.com/login.html");
            return false;

           // LoginUser.set(attribute);
            //return true;
        }

    }
}

package com.atspring.springpro.cart.interceptor;

import com.atspring.common.constant.AuthServerConstant;
import com.atspring.common.constant.CartConstant;
import com.atspring.common.vo.MemberRespVo;
import com.atspring.springpro.cart.Vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/*
拦截器：在执行目标方法之前，判断用户的登录状态，并封装传递给controller目标请求
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /*
    目标方法执行之前

     */

    @Override
    public  boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(member != null){
            //用户没登陆
            userInfoTo.setUserId(member.getId());
        }

        Cookie[] cookies = request.getCookies();
        if(cookies !=null && cookies.length>0){
            for(Cookie cookie: cookies){
                //user-key
                String name = cookie.getName();
                if(name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }

            }
        }

        //如果没有临时用户，分配一个临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /*
    业务执行之后:分配临时用户，让浏览器保存
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();

        //没有临时用户才创建
        if(!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("springpro.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}

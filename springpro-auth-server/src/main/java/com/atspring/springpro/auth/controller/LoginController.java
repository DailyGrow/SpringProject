package com.atspring.springpro.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atspring.common.constant.AuthServerConstant;
import com.atspring.common.utils.R;
import com.atspring.common.vo.MemberRespVo;
import com.atspring.springpro.auth.feign.MemberFeignService;
import com.atspring.springpro.auth.vo.UserLoginVo;
import com.atspring.springpro.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到页面使用viewcontroller
     * @return
     */
//    @GetMapping("/login.html")
//    public String loginPage(){
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String regPage(){
//        return "reg";
//    }

    /**
     * 重定向携带数据，利用session原理，将数据放在session中，只要调到下一个页面取出这个数据以后，session里面的数据就会删掉
     * //TODO 解决分布式下的session问题
     * redirectAttributes 模拟重定向携带数据
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){

        if(result.hasErrors()){

            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(
            FieldError::getField,FieldError::getDefaultMessage));

            redirectAttributes.addFlashAttribute("errors",errors);

            //用户注册->/regist[post]--->转发/reg.html(路径映射默认都是get方式访问的)所以无法转发
            //校验出错，转发到注册页
            return "redirect:http://auth.springpro.com/reg.html";
        }

        //真正注册，调用远程服务
        //校验验证码
        String code = vo.getCode();
        //System.out.println(code);
        R r = memberFeignService.regist(vo);
        if(r.getCode() == 0){
            //成功

            return "redirect:http://auth.springpro.com/login.html";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addAttribute("errors",errors);
            return "redirect:http://auth.springpro.com/reg.html";
        }

        //注册成功回到登录页
        //return "redirect:/login.html";
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){

        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            return "login";
        }else{
            return "redirect:http://springpro.com";
        }
    }

    @PostMapping("/login") //由于接的是页面，是表单kv值，所以不用requestbody
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){

        R login = memberFeignService.login(vo);
        if(login.getCode()==0){
            //成功放到session中
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>(){});
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://springpro.com";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.springpro.com/login.html";
        }
        //远程登录

    }
}

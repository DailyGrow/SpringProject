package com.atspring.springpro.auth.controller;



import com.alibaba.fastjson.TypeReference;
import com.atspring.common.utils.HttpUtils;
import com.atspring.common.utils.R;
import com.atspring.common.vo.MemberRespVo;
import com.atspring.springpro.auth.feign.MemberFeignService;
import com.atspring.springpro.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/github/success")
    public String github(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("client_id","ac23b2193e315cc721ef");
        map.put("client_secret","d54466eeb5c4ee33be24b84b66ae1a61f96e0ffc");
        map.put("code",code);
        map.put("redirect_uri","http://auth.springpro.com/oauth2.0/github/success");
        //1.根据code换取accesstoken

       HttpResponse response = HttpUtils.doPost("https://github.com","/login/oauth/access_token","post",new HashMap<>(), map, new HashMap<>());

       if(response.getStatusLine().getStatusCode()==200){
           String str = EntityUtils.toString(response.getEntity());

           String access_token = StringUtils.substringBetween(str,"access_token=","&");
           SocialUser socialUser = new SocialUser();
           socialUser.setAccess_token(access_token);
            System.out.println("封装成功");
           R oauthlogin = memberFeignService.oauthlogin(socialUser);
           if(oauthlogin.getCode()==0){
               MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
               });

               log.info("登录成功");
               //第一次使用session:服务器会命令浏览器保存卡号，JESSIONID这个COOKIE
               //以后浏览器访问会带上这个cookie，在发卡的时候指定域名为父域名,那么即使是子域系统发的卡，也能让父域直接享用
                session.setAttribute("loginUser", data);

               //2.登录成功就跳回首页
               return "redirect:http://springpro.com";
           }else{
               return "redirect:http://auth.springpro.com/login.html";
           }
           //当前用户如果是第一次进网站，自动注册
           //登录或注册这个社交用户

       }else {
           return "redirect:http://auth.springpro.com/login.html";
       }



    }
}

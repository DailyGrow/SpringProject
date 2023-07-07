package com.atspring.springpro.cart.Controller;

import com.atspring.springpro.cart.Service.CartService;
import com.atspring.springpro.cart.Vo.Cart;
import com.atspring.springpro.cart.Vo.CartItem;
import com.atspring.springpro.cart.Vo.UserInfoTo;
import com.atspring.springpro.cart.interceptor.CartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem") //处理映射请求
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.springpro.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId, num);

        return "redirect:http://cart.springpro.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check){
        cartService.checkItem(skuId, check);

        return "redirect:http://cart.springpro.com/cart.html";
    }

    /*
    浏览器有一个cookie：uer-key：标识用户身份，一个月过期
    如果第一次使用京东的购物车功能，都会给一个临时的用户身份user-key，浏览器以后保存，每次返回都会带上这个cookie

    登录：session里标识了是否登录
    没登陆：按照cookie里面带来user-key来做
    第一次，如果没有临时用户，就帮忙创建一个临时用户user-key
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

       //1,能快速得到用户信息， id/ user-key
        //ThreadLocal 同一个线程共享数据, 原理是Map<Thread, Object> threadLocal,
        //UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /*
    添加商品到购物车

    RedirectAttributes ra 重定向时携带数据
    ra.addFlashAttribute()； 将数据放在session里面可以在页面取出，但是只能取一次
    ra.addAttribute("skuId",skuId); 将数据放在url后面
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);
       // model.addAttribute("skuId",skuId);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.springpro.com/addToCartSuccess.html"; //重定向,解决重复提交的问题

    }

    /*
    跳转到成功页
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){

        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        return "success";
    }
}

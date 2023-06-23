package com.atspring.springpro.product.web;

import com.atspring.springpro.product.entity.CategoryEntity;
import com.atspring.springpro.product.service.CategoryService;
import com.atspring.springpro.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //查出所有的1级分类

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys",categoryEntities);
        //利用视图解析器进行拼串
        //classpath://templates/+返回值+.html
        return "index";
    }

    @ResponseBody //将返回的值以json方式写出去
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catelog2Vo>> getCatalogJson(){

        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;

    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1.获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        //加锁
        lock.lock(); //阻塞式等待，默认加的锁都是30s时间
        //redisson解决了1）锁的自动续期，如果业务超长，运行期间会自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉
        //2)加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除

        //lock.lock(10,TimeUnit.seconds) 在锁时间到了以后，不会自动续期，所以自动解锁时间一定要大于业务的执行时间
        //如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //如果未指定锁的超时时间，就使用看门狗的lockwatchtimeout默认时间，只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是
        // 看门狗的默认时间，每隔3分之一的看门狗时间都会走动续期，续成满时间30s】
        try{
            //执行业务
        }catch(Exception e){

        }finally{
            //解锁
            lock.unlock();
        }
        return "hello";

    }

    //读写锁保证一定能读到最新数据，在修改期间，写锁是一个排它锁（互斥锁、读享锁），读锁是一个共享锁。写锁没释放读锁就必须等待
    //写+读;等待写锁释放
    //写+写：阻塞方式
    //读+写：有读锁，写也需要等到
    //只要有写的存在，都必须等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try{

            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        }catch(InterruptedException e){
            e.printStackTrace();
        }finally{
            rLock.unlock();
        }

        return s;

    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");

        String s = "";
        RLock rLock = lock.readLock();
        rLock.lock();
        try{
            s= redisTemplate.opsForValue().get("rw-lock");
            Thread.sleep(30000);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            rLock.unlock();
        }
        return s;
    }

    /*
    信号量
    车库停车，3车位

    信号量也可以用作分布式限流
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException{
        RSemaphore park = redisson.getSemaphore("park");
        park.tryAcquire();//获取一个信号，占一个车位
        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException{
        RSemaphore park = redisson.getSemaphore("park");
        park.release(); //释放一个车位
        return "ok";
    }

    /*
    闭锁:放假，锁门，5个班全部走完，才锁门
     */

    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException{
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); //等待闭锁都完成
        return "ok";
    }

    @GetMapping("/gogogo/{id}")
    public  String gogogo(@PathVariable("id") Long id){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数减一

        return id+"ok";
    }

}

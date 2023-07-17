package com.atspring.springpro.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.Query;
import com.atspring.springpro.product.service.CategoryBrandRelationService;
import com.atspring.springpro.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atspring.springpro.product.dao.CategoryDao;
import com.atspring.springpro.product.entity.CategoryEntity;
import com.atspring.springpro.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2.组装成所有的父子分类

        //2.1)找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid()==0).
        map((menu)->{
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2)->{
            return (menu1.getSort() == null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO //1.检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);

    }

    /*
    级联更新所有关联的数据
    @CacheEvict；缓存清除,缓存一致性失效模式
    @Cacheing 同时进行多种缓存操作
     */
//    @Caching(evict={
//            @CacheEvict(value = "category",key="'getLevel1Categorys'"),
//            @CacheEvict(value="category",key="'getCatalogJson'")
//    })
    //指定删除某个分区下的所有数据 allEntries
    //存储同一类型的数据，都可以指定成同一个分区，这样可以批量修改。让分区名默认是缓存的前缀
    @CacheEvict(value="category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    //每一个需要缓存的数据我们都来指定要放到哪个名字的缓存【缓存的分区（按照业务类型分）】
    /*
    代表当前放啊的结果需要缓存,如果缓存中有，方法不用调用，如果缓存中没有，会调用方法，最后将方法的结果调入缓存
    缓存中key默认自动生成，缓存的名字SimpleKey；缓存value的值，默认使用jdk序列化机制，将序列化后的数据存到redis
    自定义：
    1）指定生成的缓存使用的key key属性指定，接受一个spel语句
    2）指定缓存的数据的存活时间 配置文件中修改ttl
    3）将数据保存为 json格式

    spring-cache的不足：
    1）读模式：
    缓存穿透：查询一个null数据。解决：缓存空数据;ache-null-valyes=true
    缓存击穿：大量并发进来同时查询一个正好过期的热点数据。解决：加锁。但springcache默认没加锁.sync = true(加本地锁，稍微解决击穿)
    缓存雪崩：大量的key同时过期。解决：加随机时间
    2）写模式(缓存与数据的一致)
    1.读写加锁
    2.引入中间件canal，感知到MySQL的更新去更新数据库
    3.读多写多，直接去数据库查询
    总结：常规数据（读多写少，及时性，一致性要求不高的数据）可以使用springcache；写模式下设置过期时间够了
    特殊数据（实时性要求高），特殊设计
     */
    @Cacheable(value={"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",0));
        return categoryEntities;
    }


    @Cacheable(value="category",key="#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList,0L);

        //分装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k->k.getCatId().toString(), v->{
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());

            List<Catelog2Vo> catelog2Vos =null;
            if(categoryEntities!=null){
                catelog2Vos = categoryEntities.stream().map(l2->{
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(),null,l2.getCatId().toString(),l2.getName());
                    //找当前二级分类的三级分类封装成vo

                    List<CategoryEntity> level3Catelog = getParent_cid(selectList,l2.getCatId());
                    if(level3Catelog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3->{
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return  catelog2Vos;
        }));
        return parent_cid;
    }

    //TODO:产生堆外内存溢出 outofDirectMemoryError
    //1)springboot默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信，lettuce的bug导致netty堆外内存溢出，没有及时释放资源
    //解决方案：可以切换使用jedis替换lettuce或是升级lettuce客户端
    //@Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2(){
        //给缓存中放json字符串，拿出的json字符串，还要逆转为能用的对象类型（序列化与反序列化）

        /*
            1.空结果缓存，解决缓存穿透
            2.设置过期时间（加随机值），解决缓存雪崩
            3.加锁：解决缓存击穿
         */
        //1.加入缓存数据,缓存中存的数据是json字符串
        //JSOn跨语言，跨平台兼容
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isEmpty(catalogJson)){
            //缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catealogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

            return catealogJsonFromDb;
        }

        //转为我们指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    /*
    缓存里面的数据如何和数据库保持一致
    缓存数据一致性
    1）双写模式：改完数据库后的数据后，更新缓存数据.会有脏数据问题影响，由于卡顿等原因，导致写缓存2在前面，写缓存1在后面就出现了不一致。这是
    短暂的脏数据问题，但是在数据稳定，缓存过期以后，又能得到最新的正确数据。双写模式得到的最新数据有延迟，但能满足最终一致性。
    2）失效模式：把数据库改完后，将缓存中数据删除，等待下一次主动查询更新。写、读并发下会出现脏数据问题
    缓存数据一致性解决方案：缓存数据+过期时间，放入缓存的数据本就不应该是实时性、一致性要求高的，应该放读多写少的数据，保证每天拿到当前最新数据即可，害怕脏数据，则通过加锁保证并发读写，适合使用读写锁，
    遇到实时性，一致性要求高的数据，就应该查数据库
    或者也可增阿基Canal中间件解决，cannal订阅binlog的方式，相当于数据库的从服务器，cannal还能解决数据异构的问题
    我们系统的一致性解决方案：
    1）缓存的所有数据都有过期时间，数据过期下一次查询触发主动更新
    2）读写数据的时候，加上分布式的读写锁

     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redisson.getLock("catalogJson-lock"); //缓存分布式锁redission
        lock.lock();

            //加锁成功，执行业务
            //避免死锁，设置过期时间,和加锁是同步的，原子的
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
           lock.unlock();
        }

        return dataFromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //占分布式锁。去redis占锁,原子加锁与解锁
        String uuid = UUID.randomUUID().toString(); //token
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",uuid,300,TimeUnit.SECONDS); //加锁使用NX EX命令
        if(lock){

            //加锁成功，执行业务
            //避免死锁，设置过期时间,和加锁是同步的，原子的
            Map<String, List<Catelog2Vo>> dataFromDb;
            try{
                dataFromDb = getDataFromDb();
            }finally {
                //获取值对比，对比成功删除，原子操作 所以用lua脚本解锁实现，脚本是要么执行完，要么都不执行
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class),Arrays.asList("lock"),uuid);
            }

            return dataFromDb;
        }else{
            //加锁失败,休眠重试
            try{
                Thread.sleep(200);
            }catch (Exception e){

            }

            return getCatalogJsonFromDbWithRedisLock();//自旋的方式
        }



    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList,0L);

        //分装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k->k.getCatId().toString(), v->{
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());

            List<Catelog2Vo> catelog2Vos =null;
            if(categoryEntities!=null){
                catelog2Vos = categoryEntities.stream().map(l2->{
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(),null,l2.getCatId().toString(),l2.getName());
                    //找当前二级分类的三级分类封装成vo

                    List<CategoryEntity> level3Catelog = getParent_cid(selectList,l2.getCatId());
                    if(level3Catelog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3->{
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return  catelog2Vos;
        }));

        //查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson",s,1,TimeUnit.DAYS);
        return parent_cid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {

        /*
        将数据库的多次查询变为一次

         */

        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //1.synchronized(this): springboot所有的组件在容器中都是单例的，this是当前实例对象，
        // TODO: 本地锁，例如synchd, JUC,只能锁当前进程,但在分布式下无法锁住所有进程,需使用分布式锁

        synchronized (this){
            //得到锁以后，应该再去缓存中确定一次，如果没有才需要继续查询
            String catalogJson = redisTemplate.opsForValue().get("catalogJson");
            if(StringUtils.isEmpty(catalogJson)) {
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return result;
            }
        }

        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList,0L);

        //分装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k->k.getCatId().toString(), v->{
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());

            List<Catelog2Vo> catelog2Vos =null;
            if(categoryEntities!=null){
                catelog2Vos = categoryEntities.stream().map(l2->{
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(),null,l2.getCatId().toString(),l2.getName());
                    //找当前二级分类的三级分类封装成vo

                    List<CategoryEntity> level3Catelog = getParent_cid(selectList,l2.getCatId());
                    if(level3Catelog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3->{
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return  catelog2Vos;
        }));

        //查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson",s,1,TimeUnit.DAYS);
        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> {
            return item.getParentCid() == parent_cid;
        }).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths){
        //1.收集当前节点的id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity->{
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //1. 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2)->{
            //2. 菜单的排序
            return (menu1.getSort() == null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}
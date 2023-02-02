package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
     private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheClient client;

    @Override
    public Result queryById(Long id) {
        //解决缓存穿透
       // Shop shop = queryOutMutex(id);
       // Shop shop = client.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //互斥锁解决缓存击穿
        // Shop shop = queryWithMutex(id);
        //逻辑过期解决缓存击穿
        //  Shop shop = queryWithLogicalExpire(id);
        Shop shop = client.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);
        return Result.ok(shop);
    }

  //  private static  final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    /*
    private Shop queryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String s = redisTemplate.opsForValue().get(key);
        //2.判断缓冲是否有数据
        if (StrUtil.isBlank(s)) {
            //3如果未命中直接返回空
            return null;
        }
        //4.命中,需要先将json反序列化为对象
        RedisData redisData = JSONUtil.toBean(s, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())){
            //5.1未过期,直接返回店铺信息
            return  shop;
        }
        //5.2 已过期,需要缓存重建
        // 6.缓存重建
        // 6.1.获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        //6.2判断是否获取锁成功
        if (isLock){
            //6.3成功,开启独立线程,实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try{
                    //重建缓存
                    this.saveShop2Redis(id,20L);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }finally {
                    unlock(lockKey);
                }
            });
        }
        //6.4返回过期的商铺信息

        return shop;
    }


     */
/*
    private Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String s = redisTemplate.opsForValue().get(key);
        //2.判断缓冲是否有数据
        if (StrUtil.isNotBlank(s)) {
            //2.1如果有数据直接返回
            Shop shop = JSONUtil.toBean(s, Shop.class);
            return shop;
        }
        if (s != null) {

            return null;
        }
        // 4.实现缓存重构
        //4.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean tryLock = tryLock(lockKey);
            // 4.2 判断是否获取成功
            if (!tryLock) {
             // 4.3 如果没有获取成功休眠
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            //4.成功,根据id查询数据库
            shop = getById(id);
            //模拟重建延时
          //  Thread.sleep(200);
            //3.1判断数据是否为空
            if (shop == null) {
                //将空值缓存到redis中
                redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                //返回错误信息
                return null;
            }
            //4.如果存在先将数据存入redis
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            unlock(lockKey);
        }
        //5.释放互斥锁

        return shop;
    }



 */

//    public void saveShop2Redis(Long id,Long expireSeconds) throws InterruptedException {
//        //1.查询店铺数据
//        Shop shop = getById(id);
//        Thread.sleep(200);
//        //2.封装逻辑过期时间
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//        //3.写入redis
//        redisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
//    }



    /*
    private Shop queryOutMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String s = redisTemplate.opsForValue().get(key);
        //2.判断缓冲是否有数据
        if (StrUtil.isNotBlank(s)) {
            //2.1如果有数据直接返回
            Shop shop = JSONUtil.toBean(s, Shop.class);
            return null;
        }
        if (s != null) {
            String s1 = redisTemplate.opsForValue().get(key);
            return null;
        }
        //3.如果没有数据就查询数据库
        Shop shop = getById(id);
        //3.1判断数据是否为空
        if (shop == null) {
            //将空值缓存到redis中
            redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //4.如果存在先将数据存入redis
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

     */

//    private boolean tryLock(String key){
//        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(aBoolean);
//    }
//    private void unlock(String key){
//        redisTemplate.delete(key);
//    }

    @Override
    @Transactional
    public Result queryUpdate(Shop shop) {
        Long id = shop.getId();
        //1.先更新数据库
        if (id == null){
            return Result.fail("id不允许为空");
        }
        updateById(shop);
        //2.再更新缓存
        redisTemplate.delete(CACHE_SHOP_KEY+id);
        return Result.ok();
    }
}

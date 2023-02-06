package com.hmdp.utils;


import cn.hutool.core.lang.UUID;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: SimpleRedisLock
 * Package: com.hmdp.utils
 * Description:
 *
 * @Author: 侯文柯
 * @Create: 2023/2/3 - 14:18
 * @Version: v1.0
 */
public class SimpleRedisLock implements ILock{
    private String name;

    private static final String KEY_PREFIX = "lock:";

   private static final String ID_PREFIX = UUID.randomUUID().toString(true)+"-";

    private StringRedisTemplate redisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    public SimpleRedisLock() {
    }
    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程唯一标识
        String id = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean success = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, id , timeoutSec, TimeUnit.SECONDS);
        //防止包装类空指针
        return Boolean.TRUE.equals(success);


    }

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT =  new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    @Override
    public void unLock() {
        //调用lua脚本
        redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());

    }


//    @Override
//    public void unLock() {
//        //获取线程标识
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//        //获取锁中的标识
//        String value = redisTemplate.opsForValue().get(KEY_PREFIX + name);
//
//        //判断标识是否一直
//        if (threadId.equals(value)){
//
//            //通过del删除锁
//            redisTemplate.delete(KEY_PREFIX + name);
//        }
//    }
}

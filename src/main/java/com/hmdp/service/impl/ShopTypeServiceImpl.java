package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.stream.util.StreamReaderDelegate;

import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public  Result queryList() {
        //1.先检查redis中是否有数据
        String shopTypeList = redisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        //2.判断是否为空
        if (StrUtil.isNotBlank(shopTypeList)){
            //2.如果不为空就直接返回
            List<ShopType> list = JSONUtil.toList(JSONUtil.parseArray(shopTypeList), ShopType.class);
            return Result.ok(list);
        }
        //3.如果为空查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList == null){
            return Result.fail("没有数据");
        }
        //4.把数据存入redis
        String jsonStr = JSONUtil.toJsonStr(typeList);
        redisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY,jsonStr);
        //5.返回数据
        return  Result.ok(typeList);
    }
}

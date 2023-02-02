package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    String tokenKey = "" ;

    @Override
    public Result senCode(String phone, HttpSession session) {

        // TODO 发送短信验证码并保存验证码
        //1.验证手机号格式
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.如果不符合,返回错误
            return Result.fail("手机号格式错误");
        }
        //发送手机验证码
        String code = RandomUtil.randomNumbers(6);
        //验证码保存到session中
      //  session.setAttribute("code", code);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //验证码保存到redis中
        //5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
       //1.检验手机号格式
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.如果不符合,返回错误
            return Result.fail("手机号格式错误");
        }
        //2.检验验证码是否正确
        // TODO 从redis中取出验证码
        String CacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
       // Object CacheCode = session.getAttribute("code");

        String code = loginForm.getCode();
        if (code == null || !CacheCode.equals(code)){
            //2.如果不符合返回错误
            return Result.fail("验证码不匹配");
        }

        //3.检查该用户在数据库是否存在
        User user = query().eq("phone", phone).one();
        //3.1 不存在注册新用户并保存到session中
        if (user == null){
            user = new User();
            user.setPhone(phone);
            user.setNickName("hmdp_"+RandomUtil.randomString(10));
            save(user);
        }
        //3.2 存在直接保存到session中
       // session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        //4.随机生成token,当做登录令牌
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //3.2 存在直接保存到redis中
        tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //3.设置token有效期
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        //返回token
        return Result.ok(token);

    }

    @Override
    public void logout() {
        stringRedisTemplate.delete(tokenKey);
    }
}

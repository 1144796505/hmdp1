package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * ClassName: LoginInterceptor
 * Package: com.hmdp.utils
 * Description:
 *
 * @Author: 侯文柯
 * @Create: 2023/2/1 - 16:52
 * @Version: v1.0
 */
//拦截器
public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    //1.判断是否要拦截
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null){
            response.setStatus(401);
            return false;
        }
        return true;
    }



}

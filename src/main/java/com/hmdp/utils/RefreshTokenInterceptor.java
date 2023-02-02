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
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate redisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取sessio,判断是否登录
//        Object user = request.getSession().getAttribute("user");
        //1.获取请求头的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return true;
        }
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        if (userMap.isEmpty()){

            return true ;
        }
        //将数据转换为userDto对象
        UserDTO userDto = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //3.如果存在,保存用户到Threadlocal
        UserHolder.saveUser(userDto);
        //刷新token过期时间
        redisTemplate.expire(LOGIN_USER_KEY + token,LOGIN_USER_TTL, TimeUnit.MINUTES);
        //放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
      UserHolder.removeUser();
    }
}

package com.hmdp;

import cn.hutool.core.util.StrUtil;
import com.hmdp.utils.SystemConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;

/**
 * ClassName: UploadTest
 * Package: com.hmdp
 * Description:
 *
 * @Author: 侯文柯
 * @Create: 2023/2/4 - 21:45
 * @Version: v1.0
 */
@SpringBootTest
public class UploadTest {
    @Test
    public void test1(){

            // 获取后缀
            String suffix = StrUtil.subAfter("originalFilename.jpg", ".", true);
            // 生成目录
            String name = UUID.randomUUID().toString();
            int hash = name.hashCode();
            int d1 = hash & 0xF;
            int d2 = (hash >> 4) & 0xF;
        System.out.println(suffix);
        System.out.println(name);
        System.out.println(hash);
        System.out.println(d1);
        System.out.println(d2);
            // 判断目录是否存在
//            File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }


    }


    @Test
    public void test2(){
        String join = StrUtil.join(",", 12, 12, 1424, 23);
        System.out.println(join);
    }
}

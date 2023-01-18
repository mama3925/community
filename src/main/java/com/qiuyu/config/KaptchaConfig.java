package com.qiuyu.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.Properties;

/**
 * @author QiuYuSY
 * @create 2023-01-18 0:31
 */
@Configuration
public class KaptchaConfig {
    @Bean
    public Producer KaptchaProducer(){
        /**
         * 手动创建properties.xml配置文件对象*
         * 设置验证码图片的样式，大小，高度，边框，字体等
         */
        Properties properties=new Properties();
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.image.width", "110");  //宽度
        properties.setProperty("kaptcha.image.height", "40");  //高度
        properties.setProperty("kaptcha.textproducer.font.size", "32"); //字号
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4"); //几个字符
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise"); //是否干扰


        DefaultKaptcha Kaptcha=new DefaultKaptcha();
        Config config=new Config(properties);
        Kaptcha.setConfig(config);

        return Kaptcha;
    }

}

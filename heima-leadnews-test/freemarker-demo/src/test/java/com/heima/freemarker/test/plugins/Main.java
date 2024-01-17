package com.heima.freemarker.test.plugins;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

@SpringBootTest
@Slf4j
public class Main {
    public static void main(String[] args) {
        String info = null;

        try {
            File pluginsDir = new File("plugins");
            File[] plugins = pluginsDir.listFiles();
            if (plugins != null) {
                for (File jar : plugins) {
                    if (jar.isFile() && jar.getName().endsWith(".jar")) {
                        URLClassLoader child = new URLClassLoader(
                                new URL[]{jar.toURI().toURL()},
                                Main.class.getClassLoader()
                        );

                        //plugins的主类名为 PluginImpl
                        Class<?> clazz = Class.forName("PluginImpl", true, child);
                        Object instance = clazz.getDeclaredConstructor().newInstance();

                        //检查实例是否为Plugin接口的实现
                        if (instance instanceof Plugin) {
                            info = ((Plugin) instance).getInfo();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("加载插件时出错: " + e.getMessage());
        }

        System.out.println(info); // 打印插件提供的信息或默认信息
    }
}

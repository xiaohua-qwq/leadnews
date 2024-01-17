package com.test;

import com.heima.file.service.FileStorageService;
import com.heima.file.service.impl.MinIOFileStorageService;
import com.xiaohuadev.testMain.MinioApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest(classes = MinioApplication.class)
@RunWith(SpringRunner.class)
public class MinIOTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() throws IOException {
        //把test.html上传到minio中 并可以在浏览器中访问
        val path = fileStorageService.uploadHtmlFile("", "test.html",
                Files.newInputStream(Paths.get("C:\\Users\\PC\\Desktop\\test.html")));

        System.out.println(path);
    }

    @Test
    public void testMinIO() {
        try {
            FileInputStream fileInputStream = new FileInputStream("C:\\Users\\PC\\Desktop\\tmp\\js\\index.js");
            MinioClient minioClient = MinioClient.builder().credentials("minioadmin", "minioadmin")
                    .endpoint("http://39.105.170.134:9090/")
                    .build();

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("plugins/js/index.js")
                    .contentType("application/javascript")
                    .bucket("leadnews")
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);

            //System.out.println("http://39.105.170.134:9090/leadnews/list.html");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

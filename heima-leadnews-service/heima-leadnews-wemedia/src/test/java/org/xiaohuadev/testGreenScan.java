package org.xiaohuadev;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class testGreenScan {

    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private GreenImageScan greenImageScan;

    @Test
    public void GreenImageScan() throws Exception {
        Map map = greenTextScan.greeTextScan("我是一个好人");
        System.out.println(map);
    }

    @Test
    public void GreenTextScan() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile
                ("http://39.105.170.134:9090/leadnews/2024/01/17/b6f22ba64f9d4e76b34b27ee2cf21d4b.jpeg");
        List<byte[]> list = new ArrayList<>();
        Map map = greenImageScan.imageScan(list);
        System.out.println(map);
    }
}

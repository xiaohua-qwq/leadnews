package org.xiaohuadev.test;

import com.heima.common.redis.CacheService;
import com.heima.schedule.ScheduleApplication;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private CacheService cacheService;

    @Test
    public void testList() {
        //cacheService.lLeftPush("test_key001", "hello world");

        String test_key001 = cacheService.lRightPop("test_key001");
        System.out.println(test_key001);
    }

    @Test
    public void testZset() {
        /*cacheService.zAdd("test_001","hello world 001",1000);
        cacheService.zAdd("test_001","hello world 002",1100);
        cacheService.zAdd("test_001","hello world 003",1200);
        cacheService.zAdd("test_001","hello world 004",2000);*/

        Set<String> resultSet = cacheService.zRangeByScore("test_001", 0, 1200);
        System.out.println(resultSet);
    }
}

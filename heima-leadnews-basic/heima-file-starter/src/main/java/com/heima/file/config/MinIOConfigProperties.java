package com.heima.file.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
public class MinIOConfigProperties implements Serializable {

    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucket = "leadnews";
    private String endpoint = "http://39.105.170.134:9090";
    private String readPath = "http://39.105.170.134:9090";
}

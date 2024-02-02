package org.xiaohuadev.kafka.pojo;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String username;
    private Integer age;
}

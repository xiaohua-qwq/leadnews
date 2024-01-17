package com.heima.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginDto {

    @ApiModelProperty(value = "手机号",required = true) //required = true为在swaggerAPI文档中将此参数标记为必须
    private String phone; //手机号

    @ApiModelProperty(value = "密码",required = true)
    private String password; //密码
}

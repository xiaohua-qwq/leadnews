package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.common.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional //事务注解
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    /**
     * app端登录功能
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //判断用户输入的信息是否为空
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            //登录判断并返回JWT
            ApUser dbUser = this.getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "账户不存在");
            }

            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String md5Password = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!md5Password.equals(dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            String jwtToken = AppJwtUtil.getToken(dbUser.getId().longValue());
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("token", jwtToken);
            dbUser.setSalt("***");
            dbUser.setPassword("***");
            responseMap.put("user", dbUser);

            return ResponseResult.okResult(responseMap);
        }

        //应该可能大概就是游客
        String guestJwtToken = AppJwtUtil.getToken(0L);
        Map<String, Object> guestResponseMap = new HashMap<>();
        guestResponseMap.put("token", guestJwtToken);
        return ResponseResult.okResult(guestResponseMap);
    }

    /*@Override
    public ResponseResult login(LoginDto dto) {
        //1.用户正常登录 需要用户名和密码
        //1.0判断传入的信息是否为空
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            //1.1根据手机号查询用户信息
            ApUser dbUser = this.getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户信息不存在");
            }

            //1.2校验用户密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String md5Password = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!md5Password.equals(dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.3返回数据 (JWT User)
            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            dbUser.setSalt("***");
            dbUser.setPassword("***");
            map.put("user", dbUser);
            return ResponseResult.okResult(map);
        }

        //2.游客登录
        Map<String, Object> map = new HashMap<>();
        map.put("token", AppJwtUtil.getToken(0L));
        return ResponseResult.okResult(map);
    }*/
}

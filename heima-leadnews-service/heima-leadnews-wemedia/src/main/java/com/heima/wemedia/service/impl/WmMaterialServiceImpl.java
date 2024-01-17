package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WimThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;


    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.检查图片是否存在
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //将图片上传到MinIO
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileURL = null;

        try {
            fileURL = fileStorageService.uploadImgFile
                    ("", fileName + postfix, multipartFile.getInputStream());
            log.info("上传文件到MinIO中: {}", fileURL);
        } catch (IOException e) {
            log.error("上传文件失败在: {}", "WmMaterialServiceImpl");
        }

        //保存到数据库
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WimThreadLocalUtils.getUser().getId());
        wmMaterial.setUrl(fileURL);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);

        //返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList(WmMaterialDto dto) {
        //1.检查参数
        dto.checkParam();

        //2.查询数据
        IPage iPage = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //2.1是否收藏
        if (dto.getIsCollection() != null && dto.getIsCollection() == 1) {
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        //2.2根据用户查找
        lambdaQueryWrapper.eq(WmMaterial::getUserId, WimThreadLocalUtils.getUser().getId());
        //2.3根据时间排序
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);

        //3.组装条件
        IPage resultPage = page(iPage, lambdaQueryWrapper);

        //4.返回结果
        ResponseResult responseResult = new PageResponseResult
                (dto.getPage(), dto.getSize(), (int) resultPage.getTotal());
        responseResult.setData(resultPage.getRecords());
        return responseResult;
    }
}

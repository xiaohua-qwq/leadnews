package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;

public interface ApUserSearchService {
    public void insert(String keyword, Integer userId);

    public ResponseResult findUserSearchHistory();

    public ResponseResult delUserSearch(HistorySearchDto dto);
}

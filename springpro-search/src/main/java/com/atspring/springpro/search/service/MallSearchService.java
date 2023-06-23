package com.atspring.springpro.search.service;

import com.atspring.springpro.search.vo.SearchParam;
import com.atspring.springpro.search.vo.SearchResult;

public interface MallSearchService {

    /*
    检索的所有参数，返回结果
     */
    SearchResult search(SearchParam param);
}

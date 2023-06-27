package com.atspring.springpro.search.Controller;

import com.atspring.springpro.search.service.MallSearchService;
import com.atspring.springpro.search.vo.SearchParam;
import com.atspring.springpro.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /*
    自动将页面提交过来的所有请求查询参数封装成指定的对象
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        param.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}

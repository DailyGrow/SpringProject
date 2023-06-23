package com.atspring.springpro.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo{

    private String catelogName;//分类名字
    private String groupName;//分组名字

    private Long[] catelogPath;
}

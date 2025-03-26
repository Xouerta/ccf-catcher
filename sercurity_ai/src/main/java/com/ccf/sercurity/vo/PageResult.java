package com.ccf.sercurity.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    //当前业
    private Integer page;
    //每页的数量
    private Integer size;
    //总共的条数
    private Long total;
    //实体类集合
    private List<T> list;
}

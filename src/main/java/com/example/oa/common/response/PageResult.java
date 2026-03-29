package com.example.oa.common.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long pageNum;    // 当前页

    private long pageSize;  // 每页条数

    private long total;     // 总条数

    private List<T> list;   // 数据列表
}

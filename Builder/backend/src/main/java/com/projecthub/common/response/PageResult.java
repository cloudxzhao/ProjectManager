package com.projecthub.common.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PageResult<T> implements Serializable {

    private List<T> list;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;

    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer size) {
        int pages = (int) Math.ceil((double) total / size);
        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .page(page)
                .size(size)
                .pages(pages)
                .build();
    }
}

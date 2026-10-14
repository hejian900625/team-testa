package com.yugi.workbench.domain.dto;

import java.util.List;

/**
 * 统一分页返回结构（契约 PageMeta + items）。
 */
public class PageResult<T> {

    private int page;
    private int size;
    private long total;
    private List<T> items;

    public PageResult() {
    }

    public PageResult(int page, int size, long total, List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}

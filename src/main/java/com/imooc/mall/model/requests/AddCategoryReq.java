package com.imooc.mall.model.requests;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Rex
 * @create 2021-02-09 12:16
 */
public class AddCategoryReq {
    @Size(min = 2, max = 5)
    @NotNull(message = "name不能为null")
    private String name;


    @NotNull(message = "orderNum不能为null")
    @Max(value = 3)
    private Integer type;

    @NotNull(message = "parentId不能为null")
    private Integer parentId;

    @NotNull(message = "orderNum不能为null")
    private Integer orderNum;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    @Override
    public String toString() {
        return "AddCategoryReq{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", parentId=" + parentId +
                ", orderNum=" + orderNum +
                '}';
    }
}

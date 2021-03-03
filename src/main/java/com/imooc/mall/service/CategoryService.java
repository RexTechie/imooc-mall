package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.model.pojo.Category;
import com.imooc.mall.model.requests.AddCategoryReq;
import com.imooc.mall.model.vo.CategoryVO;

import java.util.List;

/**
 * 分类目录Service
 * @author Rex
 * @create 2021-02-09 12:30
 */
public interface CategoryService {

    void add(AddCategoryReq addCategoryReq);

    void update(Category updateCateogry);

    void delete(Integer id);

    PageInfo<Category> listForAdmin(Integer pageNum, Integer pageSize);

    List<CategoryVO> listCategoryForCustomer(Integer parentId);
}

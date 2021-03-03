package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.model.pojo.Category;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.requests.AddCategoryReq;
import com.imooc.mall.model.requests.AddProductReq;
import com.imooc.mall.model.requests.ProductListReq;
import com.imooc.mall.model.vo.CategoryVO;

import java.util.List;

/**
 * 商品Service
 * @author Rex
 * @create 2021-02-09 12:30
 */
public interface ProductService {

    void add(AddProductReq addProductReq);

    void update(Product updateProduct);

    void delete(Integer id);

    void batchUpdateStatus(Integer[] ids, Integer updateStatus);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    Product detail(Integer id);

    PageInfo list(ProductListReq productListReq);
}

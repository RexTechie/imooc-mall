package com.imooc.mall.service;

import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.model.vo.CartVO;

import java.util.List;

/**
 * 购物车Service
 * @author Rex
 * @create 2021-02-07 14:42
 */
public interface CartService {

    List<CartVO> list(Integer userId);

    List<CartVO> add(Integer userId, Integer productId, Integer count);

    List<CartVO> delete(Integer userId, Integer productId);

    List<CartVO> update(Integer userId, Integer productId, Integer count);

    List<CartVO> selectOrNot(Integer userId, Integer productId, Integer selected);

    List<CartVO> selectAllOrNot(Integer userId, Integer selected);
}

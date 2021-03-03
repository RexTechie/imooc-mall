package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.model.requests.CreateOrderReq;
import com.imooc.mall.model.vo.CartVO;
import com.imooc.mall.model.vo.OrderVO;

import java.util.List;

/**
 * 订单Service
 * @author Rex
 * @create 2021-02-07 14:42
 */
public interface OrderService {

    String create(CreateOrderReq createOrderReq);

    OrderVO detail(String orderNo);

    PageInfo listForCustomer(Integer pageNum, Integer pageSize);

    void cancel(String orderNo);

    String qrcode(String orderNo);

    void pay(String orderNo);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    void deliver(String orderNo);

    void finish(String orderNo);
}

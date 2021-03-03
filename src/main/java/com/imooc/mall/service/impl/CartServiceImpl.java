package com.imooc.mall.service.impl;

import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.model.dao.CartMapper;
import com.imooc.mall.model.dao.ProductMapper;
import com.imooc.mall.model.pojo.Cart;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.vo.CartVO;
import com.imooc.mall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车Service实现类
 * @author Rex
 * @create 2021-02-16 10:16
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    @Override
    public List<CartVO> list(Integer userId){
        List<CartVO> cartVOS = cartMapper.selectList(userId);
        for (int i = 0; i < cartVOS.size(); i++) {
            CartVO cartVO = cartVOS.get(i);
            cartVO.setTotalPrice(cartVO.getPrice() * cartVO.getQuantity());
        }
        return cartVOS;
    }

    @Override
    public List<CartVO> add(Integer userId, Integer productId, Integer count){
        validProduct(productId, count);

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);

        if (cart == null){
            //这个商品不在购物车里，需要新增一个记录
            cart = new Cart();
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            cart.setSelected(Constant.Cart.CHECKED);
            cartMapper.insertSelective(cart);
        }else{
            //这个商品已经在购物车里了，则数量相加
            count += cart.getQuantity();
            Cart cartNew = new Cart();
            cartNew.setQuantity(count);
            cartNew.setId(cart.getId());
            cartNew.setUserId(userId);
            cartNew.setProductId(productId);
            cartNew.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateByPrimaryKeySelective(cartNew);
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> delete(Integer userId, Integer productId){
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart == null){
            //这个商品不在购物车里，无法删除
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }else{
            //这个商品已经在购物车里了，则可以删除
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> update(Integer userId, Integer productId, Integer count){
        validProduct(productId, count);

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);

        if (cart == null){
            //这个商品不在购物车里，无法更新
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }else{
            //这个商品已经在购物车里了，则数量相加
            Cart cartNew = new Cart();
            cartNew.setQuantity(count);
            cartNew.setId(cart.getId());
            cartNew.setUserId(userId);
            cartNew.setProductId(productId);
            cartNew.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateByPrimaryKeySelective(cartNew);
        }
        return this.list(userId);
    }

    private void validProduct(Integer productId, Integer count) {
        Product product = productMapper.selectByPrimaryKey(productId);
        //判断商品是否存在，商品是否上架
        if (product == null || !product.getStatus().equals(Constant.SaleStatus.SALE)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_SALE);
        }
        //判断商品库存
        if (count > product.getStock()){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH);
        }
    }

    @Override
    public List<CartVO> selectOrNot(Integer userId, Integer productId, Integer selected){
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart == null){
            //这个商品不在购物车里，无法选中
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }else{
            //这个商品已经在购物车里了，则可以选中/不选中
            cartMapper.selectOrNot(userId, productId, selected);
        }
        return cartMapper.selectList(userId);
    }


    @Override
    public List<CartVO> selectAllOrNot(Integer userId, Integer selected){
        //改变选中状态
        cartMapper.selectOrNot(userId, null, selected);
        return cartMapper.selectList(userId);
    }
}

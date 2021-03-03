package com.imooc.mall.controller;

import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.model.vo.CartVO;
import com.imooc.mall.service.CartService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车Controller
 * @author Rex
 * @create 2021-02-16 9:44
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("购物车列表")
    @GetMapping("/list")
    public ApiRestResponse list(){
        //内部获取用户id，防止横向越权
        Integer userId = UserFilter.currentUser.getId();
        List<CartVO> cartVOList = cartService.list(userId);
        return ApiRestResponse.success(cartVOList);
    }

    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public ApiRestResponse add(@RequestParam Integer productId, @RequestParam Integer count){
        Integer userId = UserFilter.currentUser.getId();
        List<CartVO> cartVOList = cartService.add(userId, productId, count);
        return ApiRestResponse.success(cartVOList);
    }

    @ApiOperation("更新购物车")
    @PostMapping("/update")
    public ApiRestResponse update(@RequestParam Integer productId, @RequestParam Integer count){
        Integer userId = UserFilter.currentUser.getId();
        List<CartVO> cartVOList = cartService.update(userId, productId, count);
        return ApiRestResponse.success(cartVOList);
    }

    @ApiOperation("删除购物车")
    @PostMapping("/delete")
    public ApiRestResponse delete(@RequestParam Integer productId){
        //不能传入userId, cartId, 否则可以删除别人的购物车
        Integer userId = UserFilter.currentUser.getId();
        List<CartVO> cartVOList = cartService.delete(userId, productId);
        return ApiRestResponse.success(cartVOList);
    }

    @ApiOperation("选择/不选择购物车的某商品")
    @PostMapping("/select")
    public ApiRestResponse select(@RequestParam Integer productId, @RequestParam Integer selected){
        //不能传入userId, cartId, 否则可以删除别人的购物车
        Integer userId = UserFilter.currentUser.getId();
        List<CartVO> cartVOList = cartService.selectOrNot(userId, productId, selected);
        return ApiRestResponse.success(cartVOList);
    }

    @ApiOperation("全选择/全不选择购物车")
    @PostMapping("/selectAll")
    public ApiRestResponse selectAll(@RequestParam Integer selected){
        //不能传入userId, cartId, 否则可以删除别人的购物车
        Integer userId = UserFilter.currentUser.getId();
        List<CartVO> cartVOList = cartService.selectAllOrNot(userId, selected);
        return ApiRestResponse.success(cartVOList);
    }


}

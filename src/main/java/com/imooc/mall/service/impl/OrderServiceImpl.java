package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.encoder.QRCode;
import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.model.dao.CartMapper;
import com.imooc.mall.model.dao.OrderItemMapper;
import com.imooc.mall.model.dao.OrderMapper;
import com.imooc.mall.model.dao.ProductMapper;
import com.imooc.mall.model.pojo.Order;
import com.imooc.mall.model.pojo.OrderItem;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.requests.CreateOrderReq;
import com.imooc.mall.model.vo.CartVO;
import com.imooc.mall.model.vo.OrderItemVO;
import com.imooc.mall.model.vo.OrderVO;
import com.imooc.mall.service.CartService;
import com.imooc.mall.service.OrderService;
import com.imooc.mall.service.ProductService;
import com.imooc.mall.service.UserService;
import com.imooc.mall.util.OrderCodeFactory;
import com.imooc.mall.util.QRCodeGenerate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单Service实现类
 * @author Rex
 * @create 2021-02-17 10:14
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;


    @Autowired
    private UserService userService;
    @Value("${file.upload.ip}")
    private String ip;

    //数据库事物
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public String create(CreateOrderReq createOrderReq){
        //拿到用户ID
        Integer userId = UserFilter.currentUser.getId();
        //从购物车查找已勾选的商品
        List<CartVO> cartVOList = cartService.list(userId);
        ArrayList<CartVO> cartVOListTemp = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            if (cartVO.getSelected().equals(Constant.Cart.CHECKED)){
                cartVOListTemp.add(cartVO);
            }
        }
        cartVOList = cartVOListTemp;
        //如果购物车已勾选的为空，报错
        if (CollectionUtils.isEmpty(cartVOList)) {
            throw new ImoocMallException(ImoocMallExceptionEnum.CART_EMPTY);
        }
        //判断商品是否存在、上下架状态、库存
        validSaleStatusAndStock(cartVOList);
        //把购物车对象转为订单item对象
        List<OrderItem> orderItemList = cartVOListToOrderItemList(cartVOList);
        //扣库存
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            Integer stock = product.getStock() - orderItem.getQuantity();
            if (stock < 0) {
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH );
            }
            product.setStock(stock);
            productMapper.updateByPrimaryKeySelective(product);
        }
        //把购物车中的商品已勾选商品删除
        cleanCart(cartVOList);
        //生成订单
        Order order = new Order();
        //生成订单号
        String orderNo = OrderCodeFactory.getOrderCode(Long.valueOf(userId));
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice(orderItemList));
        order.setReceiverName(createOrderReq.getReceiverName());
        order.setReceiverAddress(createOrderReq.getReceiverAddress());
        order.setReceiverMobile(createOrderReq.getReceiverMobile());
        order.setOrderStatus(Constant.OrderStatusEnum.NOT_PAID.getCode());
        order.setPostage(0);
        order.setPaymentType(1);
        //插入到Order表
        orderMapper.insertSelective(order);
        //循环保存每个商品到order_item表
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            orderItem.setOrderNo(order.getOrderNo());
            orderItemMapper.insertSelective(orderItem);
        }
        //把结果返回
        return orderNo;
    }

    private Integer totalPrice(List<OrderItem> orderItemList) {
        Integer totalPrice = 0;
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    private void cleanCart(List<CartVO> cartVOList) {
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            cartMapper.deleteByPrimaryKey(cartVO.getId());
        }
    }

    private List<OrderItem> cartVOListToOrderItemList(List<CartVO> cartVOList) {
        List<OrderItem> orderItemList = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartVO.getProductId());
            //记录商品快照信息
            orderItem.setProductName(cartVO.getProductName());
            orderItem.setProductImg(cartVO.getProductImage());
            orderItem.setUnitPrice(cartVO.getPrice());
            orderItem.setQuantity(cartVO.getQuantity());
            orderItem.setTotalPrice(cartVO.getTotalPrice());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    private void validSaleStatusAndStock(List<CartVO> cartVOList) {
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            Integer productId = cartVO.getProductId();
            Integer count = cartVO.getQuantity();
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
    }

    @Override
    public OrderVO detail(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //订单不存在，则报错
        if (order == null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //订单存在，需要判断所属
        Integer userId = UserFilter.currentUser.getId();
        if (!order.getUserId().equals(userId)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }
        OrderVO orderVO = getOrderVO(order);
        return orderVO;
    }

    private OrderVO getOrderVO(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        //获取订单对应的orderItemList
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, orderItemVO);
            orderItemVOList.add(orderItemVO);
        }
        orderVO.setOrderItemVOList(orderItemVOList);
        orderVO.setOrderStatusName(Constant.OrderStatusEnum.codeOf(orderVO.getOrderStatus()).getValue());
        return orderVO;
    }


    @Override
    public PageInfo listForCustomer(Integer pageNum, Integer pageSize){
        Integer userId = UserFilter.currentUser.getId();
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectForCustomer(userId);
        List<OrderVO> orderVOList = orderListToOrderVOList(orderList);
        PageInfo orderPageInfo = new PageInfo(orderList);
        orderPageInfo.setList(orderVOList);
        return orderPageInfo;
    }

    private List<OrderVO> orderListToOrderVOList(List<Order> orderList) {
        List<OrderVO> orderVOList = new ArrayList<>();
        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            OrderVO orderVO = getOrderVO(order);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    @Override
    public void cancel(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //验证用户身份
        Integer userId = UserFilter.currentUser.getId();
        if (!order.getUserId().equals(userId)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }

        if (order.getOrderStatus().equals(Constant.OrderStatusEnum.NOT_PAID.getCode())){
            order.setOrderStatus(Constant.OrderStatusEnum.CANCELD.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw new ImoocMallException(ImoocMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    @Override
    public String qrcode(String orderNo){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String address = ip +":"+ request.getLocalPort();
        String payUrl = "http://"+address+"/pay?orderNo="+orderNo;
        try {
            QRCodeGenerate.generateQRCodeImage(payUrl, 350, 350, Constant.FILE_UPLOAD_DIR+orderNo+".png");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pngAddress = "http://"+address+"/images/"+orderNo+".png";
        return pngAddress;
    }

    @Override
    public void pay(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        if (order.getOrderStatus().equals(Constant.OrderStatusEnum.NOT_PAID.getCode())){
            order.setOrderStatus(Constant.OrderStatusEnum.PAID.getCode());
            order.setPayTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw new ImoocMallException(ImoocMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }


    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllForAdmin();
        List<OrderVO> orderVOList = orderListToOrderVOList(orderList);
        PageInfo orderPageInfo = new PageInfo(orderList);
        orderPageInfo.setList(orderVOList);
        return orderPageInfo;
    }

    /**
     * 发货
     * @param orderNo
     */
    @Override
    public void deliver(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        if (order.getOrderStatus().equals(Constant.OrderStatusEnum.PAID.getCode())){
            order.setOrderStatus(Constant.OrderStatusEnum.DELIVERED.getCode());
            order.setDeliveryTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw new ImoocMallException(ImoocMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }


    /**
     * 发货
     * @param orderNo
     */
    @Override
    public void finish(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null) {
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //如果是普通用户，就要校验订单的所属
        if (!userService.checkAdminRole(UserFilter.currentUser)
                && !order.getUserId().equals(UserFilter.currentUser.getId())) {
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }

        if (order.getOrderStatus().equals(Constant.OrderStatusEnum.DELIVERED.getCode())) {
            order.setOrderStatus(Constant.OrderStatusEnum.FINISH.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        } else {
            throw new ImoocMallException(ImoocMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }
}

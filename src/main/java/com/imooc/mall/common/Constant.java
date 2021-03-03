package com.imooc.mall.common;

import com.google.common.collect.Sets;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Rex
 * @create 2021-02-08 13:56
 */
@Component
public class Constant {
    public static final String SALT = "123advaASd.';123";
    public static final String IMOOC_MALL_USER= "imooc_mall_user";

    public static String FILE_UPLOAD_DIR;

    @Value(value = "${file.upload.dir}")
    public void setFileUploadDir(String fileUploadDir){
        FILE_UPLOAD_DIR = fileUploadDir;
    }

    public interface ProductListOrderBy{
        Set<String> PRICES_ASC_DESC = Sets.newHashSet("price desc", "price asc");
    }

    public interface SaleStatus{
        int NOT_SALE = 0;//商品下架状态
        int SALE = 1;//商品下架状态
    }

    public interface Cart{
        int UN_CHECKED = 0;//购物车选中状态
        int CHECKED = 1;//购物车未选中状态
    }

    public enum OrderStatusEnum{
        CANCELD(0, "用户已取消"),
        NOT_PAID(10,  "未付款"),
        PAID(20,  "已付款"),
        DELIVERED(30,  "已发货"),
        FINISH(40,  "交易完成"),
        ;
        private int code;
        private String value;

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public static OrderStatusEnum codeOf(int code){
            for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
                if (orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ENUM);
        }
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

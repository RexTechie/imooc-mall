package com.imooc.mall.common;

import com.imooc.mall.exception.ImoocMallExceptionEnum;

/**
 * 通用返回对象
 * @author Rex
 * @create 2021-02-08 11:47
 */
public class ApiRestResponse<T> {

    private Integer status;

    private String msg;

    private T data;

    private static final Integer OK_CODE = 10000;

    private static final String OK_MSG = "SUCCESS";

    public ApiRestResponse(Integer status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public ApiRestResponse(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public ApiRestResponse() {
        this(OK_CODE, OK_MSG);
    }

    public static <T>ApiRestResponse<T> success(){
        return new ApiRestResponse<T>();
    }

    public static <T>ApiRestResponse<T> success(T result){
        ApiRestResponse<T> apiRestResponse = new ApiRestResponse<>();
        apiRestResponse.setData(result);
        return apiRestResponse;
    }

    public static <T>ApiRestResponse<T> error(Integer code, String message){
        return new ApiRestResponse<>(code, message);
    }
    public static <T>ApiRestResponse<T> error(ImoocMallExceptionEnum ex){
        return new ApiRestResponse<>(ex.getCode(), ex.getMsg());
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiRestResponse{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

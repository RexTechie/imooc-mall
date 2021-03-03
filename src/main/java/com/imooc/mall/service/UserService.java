package com.imooc.mall.service;

import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.model.pojo.User;

import java.security.NoSuchAlgorithmException;

/**
 * UserService
 * @author Rex
 * @create 2021-02-07 14:42
 */
public interface UserService {


    User getUser();

    void register(String userName, String password) throws ImoocMallException;

    User login(String userName, String password) throws ImoocMallException;

    void updateInformation(User user) throws ImoocMallException;

    boolean checkAdminRole(User user);
}

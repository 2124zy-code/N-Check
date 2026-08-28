package com.ncheck.service;

import com.ncheck.dto.LoginDTO;
import com.ncheck.dto.RegisterDTO;
import com.ncheck.vo.LoginVO;
import com.ncheck.vo.UserVO;

public interface AuthService {

    /**
     * 账号密码登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用户注册并初始化初始题库
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser();
}

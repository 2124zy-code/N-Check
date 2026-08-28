package com.ncheck.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.ncheck.common.context.UserContext;
import com.ncheck.common.context.UserInfo;
import com.ncheck.common.exception.BusinessException;
import com.ncheck.common.result.ResultCode;
import com.ncheck.dto.LoginDTO;
import com.ncheck.dto.RegisterDTO;
import com.ncheck.entity.SysUser;
import com.ncheck.entity.TbCompany;
import com.ncheck.entity.TbEntry;
import com.ncheck.mapper.SysUserMapper;
import com.ncheck.mapper.TbCompanyMapper;
import com.ncheck.mapper.TbEntryMapper;
import com.ncheck.security.JwtUtil;
import com.ncheck.service.AuthService;
import com.ncheck.vo.LoginVO;
import com.ncheck.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final TbCompanyMapper companyMapper;
    private final TbEntryMapper entryMapper;
    private final JwtUtil jwtUtil;

    private static final List<String> AVATAR_COLORS = Arrays.asList(
            "#4f46e5", "#059669", "#d97706", "#dc2626", "#8b5cf6", "#0284c7", "#06b6d4", "#e11d48"
    );

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验 BCrypt 密码
        if (!BCrypt.checkpw(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        return buildLoginResult(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        // 1. 检查用户名唯一性
        SysUser exist = userMapper.selectByUsername(dto.getUsername());
        if (exist != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // 2. 分配头像配色与哈希密码
        String color = AVATAR_COLORS.get((int) (System.currentTimeMillis() % AVATAR_COLORS.size()));
        String passwordHash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        String nickname = StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername();

        SysUser newUser = SysUser.builder()
                .username(dto.getUsername())
                .passwordHash(passwordHash)
                .nickname(nickname)
                .avatarColor(color)
                .role("USER")
                .build();

        userMapper.insert(newUser);

        return buildLoginResult(newUser);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return toUserVO(user);
    }

    private LoginVO buildLoginResult(SysUser user) {
        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();

        String token = jwtUtil.generateToken(userInfo);

        return LoginVO.builder()
                .token(token)
                .user(toUserVO(user))
                .build();
    }

    private UserVO toUserVO(SysUser user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarColor(user.getAvatarColor())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
    }
}

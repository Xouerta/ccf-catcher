package com.ccf.sercurity.service;

import com.ccf.sercurity.error.ErrorEnum;
import com.ccf.sercurity.error.PlatformException;
import com.ccf.sercurity.jwt.JwtUtils;
import com.ccf.sercurity.model.User;
import com.ccf.sercurity.repository.UserRepository;
import com.ccf.sercurity.vo.LoginRequestVO;
import com.ccf.sercurity.vo.LoginResponeVO;
import com.ccf.sercurity.vo.RegisterRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * 用户服务类
 * 负责用户管理相关操作
 */
@Service
public class UserService {

    /**
     * 用户仓库接口
     */
    private final UserRepository userRepository;
    
    /**
     * 密码编码器
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数，注入依赖
     * 
     * @param userRepository 用户仓库接口
     * @param passwordEncoder 密码编码器
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponeVO login(LoginRequestVO vo) {
        Optional<User> userOpt = userRepository.findByEmail(vo.email());
        if (userOpt.isEmpty()) {
            throw new PlatformException(ErrorEnum.USER_NOT_EXIST);
        }
        User user = userOpt.get();

        System.out.println(user);
        if (!passwordEncoder.matches(vo.password(), user.getPassword())) {
            throw new PlatformException(ErrorEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        String token = JwtUtils.generateToken(user.getId(), null, false);
        return new LoginResponeVO(token);
    }

    /**
     * 创建新用户
     * 
     * @param vo 用户信息对象
     * @return 创建的用户对象
     * @throws RuntimeException 如果用户名或邮箱已存在
     */
    public User createUser(RegisterRequestVO vo) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(vo.email())) {
            throw new PlatformException(ErrorEnum.USER_EXIST);
        }

        User user = new User();
        user.setEmail(vo.email());
        user.setUsername(vo.username());
        user.setRoles(null); // todo

        // 加密密码
        user.setPassword(passwordEncoder.encode(vo.password()));
        
        // 设置创建时间
        user.setCreatedAt(new Date());
        
        // 默认激活账户
        user.setActive(true);
        
        // 保存用户

        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     * 
     * @param email 邮箱
     * @return 用户信息（可选）
     */
    public Optional<User> findByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 更新用户的最后登录时间
     * 
     * @param username 用户名
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(new Date());
            userRepository.save(user);
        }
    }

    *//**
     * 禁用用户账号
     * 
     * @param username 用户名
     * @return 更新后的用户对象
     * @throws RuntimeException 如果用户不存在
     *//*
    public User disableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        user.setActive(false);
        return userRepository.save(user);
    }

    *//**
     * 启用用户账号
     * 
     * @param username 用户名
     * @return 更新后的用户对象
     * @throws RuntimeException 如果用户不存在
     *//*
    public User enableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        user.setActive(true);
        return userRepository.save(user);
    }

    *//**
     * 更新用户密码
     * 
     * @param username 用户名
     * @param newPassword 新密码
     * @return 更新后的用户对象
     * @throws RuntimeException 如果用户不存在
     *//*
    public User updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }*/
} 
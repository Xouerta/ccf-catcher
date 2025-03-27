package com.ccf.sercurity.service;

import com.ccf.sercurity.config.AdminConfig;
import com.ccf.sercurity.config.MailSendConfig;
import com.ccf.sercurity.config.PasswordConfig;
import com.ccf.sercurity.error.ErrorEnum;
import com.ccf.sercurity.error.PlatformException;
import com.ccf.sercurity.jwt.JwtUtils;
import com.ccf.sercurity.model.User;
import com.ccf.sercurity.model.enums.RedisPrefixEnum;
import com.ccf.sercurity.repository.UserRepository;
import com.ccf.sercurity.service.util.RedisService;
import com.ccf.sercurity.util.VerificationCodeGenerator;
import com.ccf.sercurity.vo.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 * 负责用户管理相关操作
 */
@Service
public class UserService {

    private final static String EMAIL_CODE_HTML = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>电子邮件验证</title><style>body{font-family:Arial,sans-serif;background-color:#f4f4f4;margin:0;padding:0;}.container{max-width:600px;margin:0 auto;padding:20px;background-color:#fff;border-radius:5px;box-shadow:0 2px 4px rgba(0,0,0,.1);}.code{font-size:24px;font-weight:bold;text-align:center;padding:20px 0;border-bottom:1px solid #ccc;}.note{font-size:12px;color:#999;text-align:center;margin-top:20px;}</style></head><body><div class=\"container\"><h1>电子邮件验证</h1><p>您的验证码是：</p><div class=\"code\">%s</div><p>请在验证表单中输入此代码以完成注册。</p><p class=\"note\">注意：此代码有效期为10分钟。</p></div></body></html>\n";

    @PostConstruct
    private void init() {
        Optional<User> user = this.userRepository.findByEmail(adminConfig.getEmail());
        if (user.isPresent()) {
            admin = user.get().getId();
        } else {
            User adminUser = new User();
            adminUser.setEmail(adminConfig.getEmail());
            adminUser.setUsername(adminConfig.getUsername());
            adminUser.setPassword(passwordEncoder.encode(adminConfig.getPassword()));
            adminUser.setRoles(List.of("admin"));
            adminUser.setActive(true);
            adminUser.setCreatedAt(new Date());
            admin = userRepository.save(adminUser).getId();
        }
    }

    /**
     * HTTP客户端
     */
    private final RestTemplate restTemplate;

    public static String admin;

    /**
     * 用户仓库接口
     */
    private final UserRepository userRepository;

    /**
     * 密码编码器
     */
    private final PasswordEncoder passwordEncoder;


    private final MailSendConfig mailSendConfig;


    private final AdminConfig adminConfig;

    private final RedisService redisService;

    private final PasswordConfig passwordConfig;

    /**
     * 构造函数，注入依赖
     *
     * @param userRepository  用户仓库接口
     * @param passwordEncoder 密码编码器
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AdminConfig adminConfig,
                       RedisService redisService,
                       MailSendConfig mailSendConfig,
                       PasswordConfig passwordConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminConfig = adminConfig;
        this.redisService = redisService;
        this.mailSendConfig = mailSendConfig;
        this.restTemplate = new RestTemplate();
        this.passwordConfig = passwordConfig;
    }

    public LoginResponeVO login(LoginRequestVO vo) {
        Optional<User> userOpt = userRepository.findByEmail(vo.email());
        if (userOpt.isEmpty()) {
            throw new PlatformException(ErrorEnum.USER_NOT_EXIST);
        }
        User user = userOpt.get();

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
        if (!this.checkPassword(vo.password())) {
            throw new PlatformException(ErrorEnum.WEAK_PASSWORD);
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(vo.email())) {
            throw new PlatformException(ErrorEnum.USER_EXIST);
        }
        String code = (String) redisService.get(RedisPrefixEnum.REGISTER_CODE.getPrefix() + vo.email());
        if (code == null) {
            throw new PlatformException(ErrorEnum.CODE_NO_TIME_OR_NO);
        }
        if (!code.equalsIgnoreCase(vo.code())) {
            throw new PlatformException(ErrorEnum.CODE_ERROR);
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
     * 验证码
     *
     * @param email 邮箱
     * @param type  true 注册  false 修改密码
     */
    // todo redis  login 验证码
    public void getCode(String email, boolean type) {
        boolean exists = userRepository.existsByEmail(email);
        String code = VerificationCodeGenerator.generateVerificationCode(5);
        if (redisService.get(RedisPrefixEnum.REGISTER_CODE.getPrefix() + email) != null
                || redisService.get(RedisPrefixEnum.UPDATE_PASSWORD_CODE.getPrefix() + email) != null) {
            throw new PlatformException(ErrorEnum.CODE_SEND);
        }
        if (type) {
            if (!exists) {
                mailSendConfig.send(email, "注册验证码", String.format(EMAIL_CODE_HTML, code));
                redisService.set(RedisPrefixEnum.REGISTER_CODE.getPrefix() + email, code, RedisPrefixEnum.REGISTER_CODE.getExpireTime());
                return;
            } else {
                throw new PlatformException(ErrorEnum.USER_EXIST);
            }
        }
        mailSendConfig.send(email, "修改密码验证码", String.format(EMAIL_CODE_HTML, code));
        redisService.set(RedisPrefixEnum.UPDATE_PASSWORD_CODE.getPrefix() + email, code, RedisPrefixEnum.UPDATE_PASSWORD_CODE.getExpireTime());
    }

    public UserInfoResponeVO getUserInfo(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new PlatformException(ErrorEnum.USER_NOT_EXIST);
        }
        User user = userOpt.get();
        return new UserInfoResponeVO(user.getId(), user.getUsername(), user.getCreatedAt());
    }

    public Boolean checkPassword(CheckPasswordRequestVO vo) {
        // TODO  所有weakpassword放在redis中
//        this.restTemplate
        return false;
    }

    public Boolean checkPassword(String password) {
        return this.checkPassword(new CheckPasswordRequestVO(password));
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

    */

    /**
     * 启用用户账号
     *
     * @return 更新后的用户对象
     * @throws RuntimeException 如果用户不存在
     *//*
    public User enableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        user.setActive(true);
        return userRepository.save(user);
    }

    /**
     * 更新用户密码
     *
     * @param vo request
     * @return 更新后的用户对象
     * @throws PlatformException 如果用户不存在
     */
    public void updatePassword(UpdatePasswordRequestVO vo) {
        String code = (String) redisService.get(RedisPrefixEnum.UPDATE_PASSWORD_CODE + vo.email());
        if (code == null) {
            throw new PlatformException(ErrorEnum.CODE_NO_TIME_OR_NO);
        }
        if (!code.equalsIgnoreCase(vo.code())) {
            throw new PlatformException(ErrorEnum.CODE_ERROR);
        }
        if (!this.checkPassword(vo.newPassword())) {
            throw new PlatformException(ErrorEnum.WEAK_PASSWORD);
        }

        User user = this.userRepository.findByEmail(vo.email())
                .orElseThrow(() -> new PlatformException(ErrorEnum.USER_NOT_EXIST));

        User save = new User();
        save.setId(user.getId());
        save.setPassword(passwordEncoder.encode(vo.newPassword()));
        this.userRepository.save(save);
    }

}
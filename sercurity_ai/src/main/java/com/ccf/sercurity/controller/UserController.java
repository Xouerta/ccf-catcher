package com.ccf.sercurity.controller;


import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.model.enums.SendCodeEnum;
import com.ccf.sercurity.service.UserService;
import com.ccf.sercurity.vo.LoginRequestVO;
import com.ccf.sercurity.vo.LoginResponeVO;
import com.ccf.sercurity.vo.RegisterRequestVO;
import com.ccf.sercurity.vo.UserInfoResponeVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponeVO> login(@RequestBody LoginRequestVO vo) {
        LoginResponeVO login = userService.login(vo);
        return ResponseEntity.ok(login);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestVO vo) {
        userService.createUser(vo);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查password是否为weak
     */
    @PostMapping("/checkPassword")
    @Operation(summary = "检查password是否为weak")
    public ResponseEntity<Void> checkPassword(@RequestBody String password) {
        userService.checkPassword(password);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/code")
    @Operation(summary = "获取验证码 10分钟有效   true 注册  false 修改密码")
    public ResponseEntity<String> getCode(@RequestParam("email") String email, @RequestParam("type") boolean type) {
        userService.getCode(email, type);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponeVO> userInfo(@RequestHeader("Authorization") @Token String userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @GetMapping("/code")
    public ResponseEntity<Void> getCode(@RequestParam String email, @RequestParam("type") SendCodeEnum type) {
        userService.sendCode(email, type);
        return ResponseEntity.ok().build();
    }
}

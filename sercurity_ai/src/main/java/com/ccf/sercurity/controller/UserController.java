package com.ccf.sercurity.controller;


import com.ccf.sercurity.service.UserService;
import com.ccf.sercurity.vo.LoginRequestVO;
import com.ccf.sercurity.vo.LoginResponeVO;
import com.ccf.sercurity.vo.RegisterRequestVO;
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
}

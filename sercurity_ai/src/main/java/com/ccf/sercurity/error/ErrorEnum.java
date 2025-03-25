package com.ccf.sercurity.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorEnum {

    USER_EXIST(1001, "用户已存在"),
    USER_NOT_EXIST(1002, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    UNAUTHORIZED(1004, "无权限"),
    EXIST_CATEGORY_NAME(1005, "分类名已存在"),
    TOKEN_INVALID(1006, "用户未登录"),
    FILE_SAVE_FAILED(1007, "文件保存失败"),
    NOTFOUND(1008, "资源不存在"),
    FILE_NOTFOUND(1009, "文件不存在"),
    FILE_UNKNOWN_ERROR(1010, "文件未知错误"),
    BAD_REQUEST(1011, "请求错误"),
    ARTICLE_NOT_FOUND(1012, "文章不存在"),
    COMMENT_NOT_ALLOWED(1013, "该文章不允许评论"),
    CODE_ERROR(1014, "验证码错误"),
    CODE_NO_TIME_OR_NO(1015, "验证码超时或无验证码"),
    FILE_TYPE_ERROR(1016, "文件类型错误"),
    FILE_NAME_SAME(1017, "文件重复"),
    USER_NO_EXPIRE(1018, "请重新登录"),
    CODE_SEND(1019, "注意查收"),
    TWO_COMMENT_NO_LONG_TIME(1020, "评论间隔时间过短"),
    NO_LIKE(1021, "暂未点赞"),
    FILE_TOO_SIZE(1022, "文件过大"),
    FORMAT_ERROR(1023, "请求格式错误"),
    NO_APPROVE(1024, "无审核员"),
    NO_APPROVE_REPLY(1025, "无人应答"),
    APPROVE_TOO_TIME(1026, "审核超时"),
    MEDIA_NO_APPROVE(1027, "视频未经审核"),
    NO_AVATAR(1028, "无头像"),
    MANY_LOGIN(1029, "登录频繁"),
    TAG_NO_EXIST(1030, "标签不存在"),
    ARTICLE_FAST_PUBLISH(1031, "文章发布过快"),
    MEDIA_FAST_PUBLISH(1032, "视频发布过快"),
    NET_ERROR(1033, "网络错误"),
    COMMENT_TYPE_NOT_EXIST(1034, "评论类型不存在"),
    COMMENT_NO_EXIST(1035, "评论不存在"),
    EXIST_FRIEND(1036, "好友已存在"),
    APPROVING_FRIEND(1037, "好友正在申请"),
    NOT_EXIST_FRIEND(1038, "好友不存在"),
    FRIEND_ME(1039, "不能添加自己为好友"),
    EXCEPTION_PARAM(1040, "参数异常"),
    MESSAGE_ME(1041, "不能给自己发送消息"),
    MAX_TOKEN_CARD(1042, "达到最多5端登录"),
    ;
    private final int code;
    private final String msg;
}

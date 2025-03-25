package com.ccf.sercurity.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 用户实体类
 * 存储系统用户信息
 * 映射到Elasticsearch的users索引
 */
@Data
@Document(indexName = "users")
public class User {
    
    /**
     * 用户唯一标识符
     */
    @Id
    private String id;
    
    /**
     * 用户名，用于登录
     */
    @Field(type = FieldType.Keyword)
    private String username;
    
    /**
     * 密码（加密存储）
     */
    @Field(type = FieldType.Text)
    private transient String password;
    
    /**
     * 用户邮箱
     */
    @Field(type = FieldType.Keyword)
    private String email;
    
    /**
     * 用户角色/权限列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> roles;
    
    /**
     * 账户创建时间
     */
    @Field(type = FieldType.Date)
    private Date createdAt;
    
    /**
     * 最后登录时间
     */
    @Field(type = FieldType.Date)
    private Date lastLogin;
    
    /**
     * 账户是否激活
     */
    @Field(type = FieldType.Boolean)
    private boolean active;
} 
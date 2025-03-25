package com.ccf.sercurity.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@ConfigurationProperties(prefix = "ccf.security")
@SuppressWarnings({"unused", "deprecation"})
public class JwtUtils {
    private static byte[] key;

    /**
     * 根据用户名和用户角色生成 token
     *
     * @param id         id
     * @param isRemember 是否记住我
     * @param role       角色
     * @return 返回生成的 token
     */
    public static String generateToken(String id, String role, boolean isRemember) {
        // 过期时间
        long expiration = isRemember ? SecurityConstants.EXPIRATION_REMEMBER_TIME : SecurityConstants.EXPIRATION_TIME;
        // 生成 token
        return Jwts.builder()
                // 生成签证信息
//                .setHeaderParam("typ", SecurityConstants.TOKEN_TYPE)
                .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
                .setSubject(String.valueOf(id))
                .claim(SecurityConstants.TOKEN_ROLE_CLAIM, role)
                .setIssuer(SecurityConstants.TOKEN_ISSUER)
                .setIssuedAt(new Date())
//                .setAudience(SecurityConstants.TOKEN_AUDIENCE)
                // 设置有效时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .compact();
    }

    /**
     * 验证 token 是否有效
     *
     * <p>
     * 如果解析失败，说明 token 是无效的
     *
     * @param token token 信息
     * @return 如果返回 true，说明 token 有效
     */
    public static boolean validateToken(String token) {
        try {
            getTokenBody(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Request to parse expired JWT : {} failed : {}", token, e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Request to parse unsupported JWT : {} failed : {}", token, e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Request to parse invalid JWT : {} failed : {}", token, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Request to parse empty or null JWT : {} failed : {}", token, e.getMessage());
        }
        return false;
    }

    /**
     * 根据 token 获取用户认证信息
     *
     * @param token token 信息
     * @return 返回用户认证信息
     */
    public static Authentication getAuthentication(String token) {
        Claims claims = getTokenBody(token);
        return new JwtAuthenticationToken(claims.getSubject());
    }

    private static Claims getTokenBody(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public static Object getTokenRole(String token) {
        return getTokenBody(token).get(SecurityConstants.TOKEN_ROLE_CLAIM);
    }

    public static Long getTokenId(String token) {
        token = token.substring(7);
        return Long.parseLong(getTokenBody(token).getSubject());
    }

    private byte[] getKey() {
        return key;
    }

    public void setKey(String key) {
        JwtUtils.key = key.getBytes();
    }
}

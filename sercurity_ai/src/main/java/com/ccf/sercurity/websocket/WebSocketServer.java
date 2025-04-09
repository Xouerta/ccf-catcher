package com.ccf.sercurity.websocket;

import com.ccf.sercurity.error.PlatformException;
import com.ccf.sercurity.model.enums.WebsocketTypeEnum;
import com.ccf.sercurity.service.WebsocketService;
import com.ccf.sercurity.vo.WebsocketPushVO;
import com.ccf.sercurity.vo.WebsocketScannerVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@SuppressWarnings("all")
@RequiredArgsConstructor
@ServerEndpoint("/websocket/{userId}")
public class WebSocketServer {
    private static WebsocketService websocketService = null;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static AtomicLong onlineCount = new AtomicLong(0L);

    private String userId;

    public static void getContext(ApplicationContext context) {
        websocketService = context.getBean(WebsocketService.class);
    }

    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) throws IOException {

        this.userId = userId;
        sessionMap.put(userId, session);
        onlineCount.incrementAndGet();

        log.info("websocket连接成功 {} 总计 {}", userId, onlineCount.get());
    }


    @OnMessage
    public void onMessage(String message, Session session) throws JsonProcessingException {
        log.info("websocket收到消息 {} {}", userId, message);

        websocketService.parseWebsocketEntity(objectMapper.readValue(message, WebsocketScannerVO.class));
    }

    @OnClose
    public void onClose() {
        sessionMap.remove(userId);
        log.info("websocket连接关闭 {} 总计 {}", userId, onlineCount.decrementAndGet());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket连接异常 {} {}", userId, error.getMessage());
        WebsocketPushVO<Object> vo;
        if (error instanceof PlatformException) {
            vo = new WebsocketPushVO<>().setCode(HttpStatus.BAD_REQUEST.value()).setType(WebsocketTypeEnum.ERROR.getType()).setData(((PlatformException) error).getMsg());
        } else {
            vo = new WebsocketPushVO<>().setCode(HttpStatus.BAD_REQUEST.value()).setType(WebsocketTypeEnum.ERROR.getType()).setData("异常操作");
        }
        error.printStackTrace(); // TODO delete
        session.getAsyncRemote().sendText(objectToString(vo));
    }

    public static <T> void sendMessage(String userId, WebsocketPushVO<T> message) {
        Session session = sessionMap.get(userId);
        if (session != null) {
            session.getAsyncRemote().sendText(objectToString(message));
        }
    }

    public static <T> void sendMessage(WebsocketPushVO<T> message) {
        sessionMap.forEach((userId, session) -> {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(objectToString(message));
            }
        });
    }

    public static Boolean userOnLine(String userId) {
        return sessionMap.containsKey(userId);
    }

    private static String objectToString(Object object) {
        String str = null;
        try {
            str = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ignored) {
        }
        return str;
    }
}

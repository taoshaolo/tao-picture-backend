package com.taoshao.taopicture.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.taoshao.taopicture.manager.websocket.disruptor.PictureEditEventProducer;
import com.taoshao.taopicture.manager.websocket.model.PictureEditActionEnum;
import com.taoshao.taopicture.manager.websocket.model.PictureEditMessageTypeEnum;
import com.taoshao.taopicture.manager.websocket.model.PictureEditRequestMessage;
import com.taoshao.taopicture.manager.websocket.model.PictureEditResponseMessage;
import com.taoshao.taopicture.model.entity.User;
import com.taoshao.taopicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author taoshao
 * @Date 2025/1/13
 */
@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 首次加入编辑，初始化空的集合
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        // 构造响应消息， 并发送加入编辑的消息
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户【%s】加入编辑", user.getUserName());
        responseMessage.setMessage(message);
        responseMessage.setUser(userService.getUserVO(user));
        // 广播给该图片的所有用户
        broadcastToPicture(pictureId, responseMessage);
    }

    /**
     * 收到前端发送的消总，根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 将消息解析为 PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        // 从 Session 属性中获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        // 根据消息类型处理消息（生产消息到 Disruptor 环形队列中）
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
    }

    /**
     * 进入编辑
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        // 没有用户正在编辑该图片，才进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 设置当前用户正在编辑该图片
            pictureEditingUsers.put(pictureId, user.getId());
            // 构造响应消息， 并发送加入编辑的消息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户【%s】开始编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给该图片的所有用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 处理编辑操作
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 正在编辑的用户
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum enumByValue = PictureEditActionEnum.getEnumByValue(editAction);
        if (enumByValue == null) {
            log.error("无效的编辑动作");
            return;
        }
        // 确认是当前的编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 构造响应消息， 并发送编辑动作的消息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("用户【%s】执行了【%s】操作", user.getUserName(), enumByValue.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            // 广播给除了当前用户之外的所有用户，否则会造成重复编辑
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 退出编辑
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 正在编辑的用户
        Long editingUserId = pictureEditingUsers.get(pictureId);
        // 确认是当前的编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 移除当前用户正在编辑状态
            pictureEditingUsers.remove(pictureId);
            // 构造响应消息， 并发送退出编辑的消息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户【%s】退出编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给该图片的所有用户
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 连接关闭
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 从 Session 中获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 移除当前用户正在编辑状态
        handleExitEditMessage(null, session, user, pictureId);
        // 删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (CollUtil.isEmpty(sessionSet)) {
                pictureSessions.remove(pictureId);
            }
        }
        // 通知所有用户，该用户退出了编辑操作
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
        String message = String.format("用户【%s】退出编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        // 广播给该图片的所有用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }


    /**
     * 广播到该图片的所以用户
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param excludeSession
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化： 将 Long 转为 String，避免精度丢失
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                if (excludeSession != null && session.equals(excludeSession)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    // 全部广播
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }


}

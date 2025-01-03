package com.taoshao.taopicture.manager;


import com.taoshao.taopicture.common.ErrorCode;
import com.taoshao.taopicture.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 AI 调用
 *
 * @Author taoshao
 * @Date 2024/9/9
 */
//@Component
public class AiManager {

    @Resource
    private ClientV4 clientV4;
    @Resource
    private YuCongMingClient yuCongMingClient;

    private static final Long DEFAULT_AI_ASSISTANT_ID = 1654785040361893889L;

    /**
     * 智谱 AI 默认通用请求
     * @param userMessage
     * @return
     */
    public String doRequest(String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return (String) invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
    /**
     * 鱼聪明 AI
     * 自定义 AI 助手 对话
     * @@param modelId
     * @param message
     * @return
     */
    public String doChatCustomAi(long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }

    /**
     * 鱼聪明 AI
     * 默认 AI 助手 对话
     * @param message
     * @return
     */
    public String doChatDefaultAi( String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(DEFAULT_AI_ASSISTANT_ID);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }
}

package com.github.tornado2023team5.kanjichan.util;

import com.github.tornado2023team5.kanjichan.model.function.FunctionCallingBase;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FunctionCallUtil {
    private final OpenAiService service;

    public <T> T call(String text, FunctionCallingBase base) {
        var executor = base.getExecutor();
        var request = base.getRequest(executor);
        request.setMessages(List.of(new ChatMessage(ChatMessageRole.USER.value(), text)));
        ChatMessage responseMessage = service.createChatCompletion(request).getChoices().get(0).getMessage();
        ChatFunctionCall functionCall = responseMessage.getFunctionCall();
        return executor.execute(functionCall);
    }
}

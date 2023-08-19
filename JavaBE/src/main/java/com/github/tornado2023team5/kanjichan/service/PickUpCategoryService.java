package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.model.ShopCategory;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PickUpCategoryService {
    private final FunctionExecutor executor;
    private final OpenAiService service;
    private final ChatCompletionRequest request;
    private static final List<ChatMessage> baseMessages = Arrays.asList(
            new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful bot that helps people find restaurant.")
    );

    public static List<ChatMessage> getMessages(String text) {
        var messages = new ArrayList<>(baseMessages);
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), text));
        return messages;
    }

    public ShopCategory pickUp(String text) {
        request.setMessages(getMessages(text));
        ChatMessage responseMessage = service.createChatCompletion(request).getChoices().get(0).getMessage();
        ChatFunctionCall functionCall = responseMessage.getFunctionCall();

        ChatMessage functionResponseMessage = executor.executeAndConvertToMessageHandlingExceptions(functionCall);
        ShopCategory info = executor.execute(functionCall);
        System.out.println(info);
        System.out.println(functionResponseMessage);
        return executor.execute(functionCall);
    }
}

package com.github.tornado2023team5.kanjichan.service.shop;

import com.github.tornado2023team5.kanjichan.model.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.ShopInfo;
import com.github.tornado2023team5.kanjichan.model.ShopInfoSource;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PickUpShopService {
    private final FunctionExecutor executor;
    private final OpenAiService service;
    private final ChatCompletionRequest request;
    private static final List<ChatMessage> baseMessages = Arrays.asList(
            new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful bot that helps people find restaurant.")
    );

    @Bean
    public FunctionExecutor getExecutor() {
        var functions = Arrays.asList(
                //        ChatFunction.builder()
//                .name("get_shop_info_from_url")
//                .description("お店のURLからお店の情報を取得します。")
//        ;

                ChatFunction.builder().name("get_shop_info_from_text")
                        .description("自然言語からお店の情報を取得します。")
                        .executor(ShopInfoSource.class, (input) -> input)
                        .build(),


                ChatFunction.builder().name("get_shop_category_from_text")
                        .description("お店のガテゴリからお店の情報を取得します。")
                        .executor(ShopCategory.class, CategoryService::getShopInfoFromCategory)
                        .build()
        );
        var executor = new FunctionExecutor(functions);
        return executor;
    }

    @Bean
    public ChatCompletionRequest getRequest(FunctionExecutor executor) {
        return ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .functions(executor.getFunctions())
                .functionCall(new ChatCompletionRequest.ChatCompletionRequestFunctionCall("auto"))
                .maxTokens(256)
                .build();
    }

    public static List<ChatMessage> getMessages(String text) {
        var messages = new ArrayList<>(baseMessages);
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), text));
        return messages;
    }

    public ShopInfo pickUp(String text) {
        request.setMessages(getMessages(text));
        ChatMessage responseMessage = service.createChatCompletion(request).getChoices().get(0).getMessage();
        ChatFunctionCall functionCall = responseMessage.getFunctionCall();

        ChatMessage functionResponseMessage = executor.executeAndConvertToMessageHandlingExceptions(functionCall);
        ShopInfo info = executor.execute(functionCall);
        System.out.println(info);
        System.out.println(functionResponseMessage);
        return executor.execute(functionCall);
    }
}

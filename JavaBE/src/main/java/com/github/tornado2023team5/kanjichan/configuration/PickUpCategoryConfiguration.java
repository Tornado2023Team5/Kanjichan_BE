package com.github.tornado2023team5.kanjichan.configuration;

import com.github.tornado2023team5.kanjichan.model.ShopCategory;
import com.github.tornado2023team5.kanjichan.service.CategoryService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.service.FunctionExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class PickUpCategoryConfiguration {
    private final CategoryService categoryService;

    @Bean
    public FunctionExecutor getExecutor() {
        var functions = Arrays.asList(
                //        ChatFunction.builder()
//                .name("get_shop_info_from_url")
//                .description("お店のURLからお店の情報を取得します。")
//        ;

                ChatFunction.builder().name("get_shop_category_from_text")
                        .description("自然言語からお店のカテゴリを取得します。")
                        .executor(ShopCategory.class, (input) -> input)
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
                .functionCall(new ChatCompletionRequest.ChatCompletionRequestFunctionCall("get_shop_category_from_text"))
                .maxTokens(256)
                .build();
    }
}

package com.github.tornado2023team5.kanjichan.model.function;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import lombok.Data;
import lombok.Value;
import lombok.With;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Value
@With
public class FunctionCallingBase {
    Class clazz;
    String functionName;
    String functionDescription;
    String baseMessages;

    public FunctionExecutor getExecutor() {
        return new FunctionExecutor(Collections.singletonList(
                ChatFunction.builder().name(functionName)
                        .description(functionDescription)
                        .executor(clazz, (input) -> input)
                        .build()
        ));
    }


    public ChatCompletionRequest getRequest(FunctionExecutor executor) {
        return ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .functions(executor.getFunctions())
                .messages(List.of(new ChatMessage(ChatMessageRole.SYSTEM.value(), baseMessages)))
                .functionCall(new ChatCompletionRequest.ChatCompletionRequestFunctionCall(functionName))
                .maxTokens(256)
                .build();
    }
}

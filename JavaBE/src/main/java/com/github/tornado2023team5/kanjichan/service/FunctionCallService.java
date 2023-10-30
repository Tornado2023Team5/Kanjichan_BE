package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.function.*;
import com.github.tornado2023team5.kanjichan.model.function.command.*;
import com.github.tornado2023team5.kanjichan.util.FunctionCallUtil;
import com.google.maps.model.PlacesSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FunctionCallService {
    private final FunctionCallUtil functionCallUtil;

    public CommandInformationFormat detect(String text, String prompt) {
        CommandTypeFormat format = functionCallUtil.call(text, FunctionType.DETECT_INPUT_FOR_COMMAND.getBase().withBaseMessages(prompt));
        return new CommandInformationFormat(format.getCommandType(), text);
    }

    public ShopCategory pickup(String text) {
        return functionCallUtil.call(text, FunctionType.GET_SHOP_CATEGORY_FROM_TEXT.getBase());
    }

    public MakePlanCommand makePlan(String text) {
        return functionCallUtil.call(text, FunctionType.MAKE_PLAN.getBase());
    }

    public SetLocationCommand setLocation(String text) {
        return functionCallUtil.call(text, FunctionType.SET_LOCATION.getBase());
    }

    public SetLocationCommand setTime(String text) {
        return functionCallUtil.call(text, FunctionType.SET_TIME.getBase());
    }

    public SearchSpotCommand searchSpots(String text) {
        return functionCallUtil.call(text, FunctionType.SEARCH_SPOT.getBase());
    }

    public RemoveSpotCommand removeSpot(String text, List<PlacesSearchResult> spots) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力を基に下記のスポットから削除するものを候補にある名前をそのままで選択してください。
                下記のスポット一覧以外の入力は無効です。
                ###
                """ + spots.stream().map(spot -> spot.name).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, FunctionType.REMOVE_SPOT.getBase().withBaseMessages(prompt));
    }

    public DecideDraftCommand decideDraft(String text, List<List<Action>> drafts) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きから一つ選択してください。
                ###
                                
                """;
        for (int i = 0; i < drafts.size(); i++) {
            prompt += "草案" + i + "\n" + drafts.get(i).stream().map(Action::getName).collect(Collectors.joining("\n")) + "\n";
        }
        return functionCallUtil.call(text, FunctionType.DECIDE_DRAFT.getBase().withBaseMessages(prompt));
    }

    public EditAndAddSpotFromDecidedDraftCommand editAndAddSpotToDecidedDraft(String text, List<Action> draft) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きにスポットを追加してください。
                ###
                """;
        prompt += draft.stream().map(Action::getName).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, FunctionType.EDIT_AND_ADD_SPOT_FROM_DECIDED_DRAFT.getBase().withBaseMessages(prompt));
    }

    public EditAndRemoveSpotFromDecidedDraftCommand editAndRemoveSpotFromDecidedDraft(String text, List<Action> draft) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きからスポットを削除してください。
                ###
                """;
        prompt += draft.stream().map(Action::getName).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, FunctionType.EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT.getBase().withBaseMessages(prompt));
    }

    public EditAndChangeSpotFromDecidedDraftCommand editAndChangeSpotFromDecidedDraft(String text, List<Action> draft) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きのスポットの順番を入れ替えてください。
                ###
                """;
        prompt += draft.stream().map(Action::getName).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, FunctionType.EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT.getBase().withBaseMessages(prompt));
    }
}

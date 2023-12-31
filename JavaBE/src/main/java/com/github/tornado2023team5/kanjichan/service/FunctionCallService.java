package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.function.CommandInformationFormat;
import com.github.tornado2023team5.kanjichan.model.function.CommandTypeFormat;
import com.github.tornado2023team5.kanjichan.model.function.FunctionCallingBase;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
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
        CommandTypeFormat format = functionCallUtil.call(text, new FunctionCallingBase(CommandTypeFormat.class, "detect_input_for_command", "分類したコマンドを返す関数。ここでプログラマーはコマンドの入力内容を取得します。 ", prompt));
        return new CommandInformationFormat(format.getCommandType(), text);
    }

    public ShopCategory pickup(String text) {
        return functionCallUtil.call(text, new FunctionCallingBase(ShopCategory.class, "get_shop_category_from_text", "自然言語からお店のカテゴリを取得します。", "You are a helpful bot that helps people find restaurant."));
    }

    public MakePlanCommand makePlan(String text) {
        return functionCallUtil.call(text, new FunctionCallingBase(MakePlanCommand.class, "make_plan", "遊び計画を作成します。", "You are a helpful bot that helps people make a plan."));
    }

    public SetLocationCommand setLocation(String text) {
        return functionCallUtil.call(text, new FunctionCallingBase(SetLocationCommand.class, "set_location", "目的地を設定します。", "You are a helpful bot that helps people set a location for a travel plan."));
    }

    public SearchSpotCommand searchSpots(String text) {
        return functionCallUtil.call(text, new FunctionCallingBase(SearchSpotCommand.class, "search_spots", "観光スポットを検索します。", "You are a helpful bot that helps people search a spot for a travel plan."));
    }

    public RemoveSpotCommand removeSpot(String text, List<PlacesSearchResult> spots) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力を基に下記のスポットから削除するものを候補にある名前をそのままで選択してください。
                下記のスポット一覧以外の入力は無効です。
                ###
                """ + spots.stream().map(spot -> spot.name).collect(Collectors.joining("\n"));
        System.out.println(prompt);
        return functionCallUtil.call(text, new FunctionCallingBase(RemoveSpotCommand.class, "remove_spot", "観光スポットを削除します。スポット名のリストを入れてください", prompt));
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
        return functionCallUtil.call(text, new FunctionCallingBase(DecideDraftCommand.class, "decide_draft", "下書きを選択します。", prompt));
    }

    public EditAndAddSpotFromDecidedDraftCommand editAndAddSpotToDecidedDraft(String text, List<Action> draft) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きにスポットを追加してください。
                ###
                """;
        prompt += draft.stream().map(Action::getName).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, new FunctionCallingBase(EditAndAddSpotFromDecidedDraftCommand.class, "edit_and_add_spot_to_decided_draft", "下書きにスポットを追加します。", prompt));
    }

    public EditAndRemoveSpotFromDecidedDraftCommand editAndRemoveSpotFromDecidedDraft(String text, List<Action> draft) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きからスポットを削除してください。
                ###
                """;
        prompt += draft.stream().map(Action::getName).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, new FunctionCallingBase(EditAndRemoveSpotFromDecidedDraftCommand.class, "edit_and_remove_spot_from_decided_draft", "下書きからスポットを削除します。", prompt));
    }

    public EditAndChangeSpotFromDecidedDraftCommand editAndChangeSpotFromDecidedDraft(String text, List<Action> draft) {
        String prompt = """
                あなたは旅行者の旅行計画を補助するBOTです。
                ユーザーの入力内容を基にして以下の下書きのスポットの順番を入れ替えてください。
                ###
                """;
        prompt += draft.stream().map(Action::getName).collect(Collectors.joining("\n"));
        return functionCallUtil.call(text, new FunctionCallingBase(EditAndChangeSpotFromDecidedDraftCommand.class, "edit_and_change_spot_from_decided_draft", "下書きのスポットの順番を入れ替えます。", prompt));
    }
}

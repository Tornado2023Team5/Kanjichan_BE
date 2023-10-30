package com.github.tornado2023team5.kanjichan.model.function;

import com.github.tornado2023team5.kanjichan.model.function.command.*;
import com.github.tornado2023team5.kanjichan.util.FunctionCallUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public enum FunctionType {
    DETECT_INPUT_FOR_COMMAND,
    GET_SHOP_CATEGORY_FROM_TEXT,
    MAKE_PLAN,
    SET_LOCATION,
    SET_TIME,
    SEARCH_SPOT,
    REMOVE_SPOT,
    DECIDE_DRAFT,
    EDIT_AND_ADD_SPOT_FROM_DECIDED_DRAFT,
    EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT,
    EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT;

    private FunctionCallingBase base;

    static {
        DETECT_INPUT_FOR_COMMAND.base = new FunctionCallingBase(CommandTypeFormat.class, FunctionType.DETECT_INPUT_FOR_COMMAND.name(), "分類したコマンドを返す関数。ここでプログラマーはコマンドの入力内容を取得します。 ", null);
        GET_SHOP_CATEGORY_FROM_TEXT.base = new FunctionCallingBase(ShopCategory.class, FunctionType.GET_SHOP_CATEGORY_FROM_TEXT.name(), "自然言語からお店のカテゴリを取得します。", "You are a helpful bot that helps people find restaurant.");
        MAKE_PLAN.base = new FunctionCallingBase(MakePlanCommand.class, FunctionType.MAKE_PLAN.name(), "遊び計画を作成します。", "You are a helpful bot that helps people make a plan.");
        SET_LOCATION.base = new FunctionCallingBase(SetLocationCommand.class, FunctionType.SET_LOCATION.name(), "目的地を設定します。", "You are a helpful bot that helps people set a location for a travel plan.");
        SET_TIME.base = new FunctionCallingBase(SetLocationCommand.class, FunctionType.SET_TIME.name(), "目的地を設定します。", "You are a helpful bot that helps people set a location for a travel plan.");
        SEARCH_SPOT.base = new FunctionCallingBase(SearchSpotCommand.class, FunctionType.SEARCH_SPOT.name(), "観光スポットを検索します。", "You are a helpful bot that helps people search a spot for a travel plan.");
        REMOVE_SPOT.base = new FunctionCallingBase(RemoveSpotCommand.class, FunctionType.REMOVE_SPOT.name(), "観光スポットを削除します。スポット名のリストを入れてください", null);
        DECIDE_DRAFT.base = new FunctionCallingBase(DecideDraftCommand.class, FunctionType.DECIDE_DRAFT.name(), "下書きを決定します。", null);
        EDIT_AND_ADD_SPOT_FROM_DECIDED_DRAFT.base = new FunctionCallingBase(EditAndAddSpotFromDecidedDraftCommand.class, FunctionType.EDIT_AND_ADD_SPOT_FROM_DECIDED_DRAFT.name(), "下書きを決定した後にスポットを追加します。", null);
        EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT.base = new FunctionCallingBase(EditAndChangeSpotFromDecidedDraftCommand.class, FunctionType.EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT.name(), "下書きを決定した後にスポットを変更します。", null);
        EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT.base = new FunctionCallingBase(EditAndRemoveSpotFromDecidedDraftCommand.class, FunctionType.EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT.name(), "下書きを決定した後にスポットを削除します。", null);
    }
}

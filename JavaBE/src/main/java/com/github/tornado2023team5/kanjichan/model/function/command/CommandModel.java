package com.github.tornado2023team5.kanjichan.model.function.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public sealed class CommandModel permits DecideDraftCommand, EditAndAddSpotFromDecidedDraftCommand, EditAndChangeSpotFromDecidedDraftCommand, EditAndRemoveSpotFromDecidedDraftCommand, MakePlanCommand, RemoveSpotCommand, SearchSpotCommand, SetLocationCommand {
}

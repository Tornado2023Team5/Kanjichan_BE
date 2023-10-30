package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.model.function.command.MakePlanCommand;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
public class CommandMakePlanService {
    private final SetupScheduleService setupScheduleService;
    private final CommandSetDestinationService commandSetDestinationService;
    private final CommandSearchSpotsService commandSearchSpotsService;
    private final LineMessagingClient lineMessagingClient;

    public void makePlan(MakePlanCommand command, String id, String lineId, StringBuilder reply) throws IOException, InterruptedException, ApiException, ExecutionException {
        setupScheduleService.start(id, lineId, lineMessagingClient.getGroupSummary(id).get().getGroupName());
        reply.append("予定を立てる準備をしたウサ！🥕\n");

        if (command.getDestination() != null)
            commandSetDestinationService.setDestination(id, reply, command.getDestination(), false);
        else {
            reply.append("集合場所を教えるウサ！🥕\n");
            return;
        }

        if (command.getCategory() != null)
            commandSearchSpotsService.searchSpots(id, reply, command.getCategory(), setupScheduleService.getSession(id).getLocation());
        else reply.append("何をして遊ぶかを教えるウサ！🥕\n");
    }
}

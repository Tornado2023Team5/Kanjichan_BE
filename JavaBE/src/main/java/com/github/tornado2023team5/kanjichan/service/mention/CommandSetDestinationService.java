package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommandSetDestinationService {
    private final SetupScheduleService setupScheduleService;


    public void setDestination(String id, StringBuilder reply, String destination, boolean sendReply) {
        setupScheduleService.setLocation(id, destination);
        if (sendReply) reply.append("活動場所を「").append(destination).append("」に設定したウサ！🥕\n");
    }
}

package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommandShowAdoptedSpotsService {
    private final SetupScheduleService setupScheduleService;

    public void showAdoptedSpots(String id, StringBuilder reply) {
        var session = setupScheduleService.getSession(id);
        reply.append("今はここで遊ぶ予定を立てているウサ！🥕");
        for (var results : session.getResultsList()) {
            for (var result : results) {
                reply.append(result.name).append("\n");
            }
            reply.append("\n");
        }
    }
}

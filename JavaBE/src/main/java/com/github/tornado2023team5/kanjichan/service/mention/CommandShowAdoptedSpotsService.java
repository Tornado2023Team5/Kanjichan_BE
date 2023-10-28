package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommandShowAdoptedSpotsService {
    private final SetupScheduleService setupScheduleService;

    public void showAdoptedSpots(String id, StringBuilder reply) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResultsList().size() == 0) {
            reply.append("遊ぶ内容を教えてほしいウサ！🥕");
            return;
        }
        reply.append("今はここで遊ぶ予定を立てているウサ！🥕");
        for (var results : session.getResultsList()) {
            for (var result : results) {
                reply.append(result.name).append("\n");
            }
            reply.append("\n");
        }
    }
}

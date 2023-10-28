package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommandResetPlanService {
    private final SetupScheduleService setupScheduleService;

    public void resetPlan(String id, StringBuilder reply) {
        setupScheduleService.reset(id);
        reply.append("編集中の予定をリセットして、全ての情報を削除したウサ🥕\n");
    }
}

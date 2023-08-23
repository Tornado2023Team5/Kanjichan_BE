package com.github.tornado2023team5.kanjichan.repository;

import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.model.ShopInfo;
import com.github.tornado2023team5.kanjichan.util.RestfulAPIUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleRepository {
    private final RestfulAPIUtil restfulAPIUtil;

    public void save(AsobiPlanningSession session) {
        var asobi = new Asobi();
        asobi.setId(session.getId());
        asobi.setParticipants(session.getUsers());
        asobi.setName("asobi");
        asobi.setDescription("asobi");
        asobi.setActions(session.getActions());
        restfulAPIUtil.post("http://localhost:4000/api/asobi", asobi);
    }
}

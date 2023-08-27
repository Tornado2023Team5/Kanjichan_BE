package com.github.tornado2023team5.kanjichan.repository;

import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.util.RestfulAPIUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleRepository {
    private final RestfulAPIUtil restfulAPIUtil;


}

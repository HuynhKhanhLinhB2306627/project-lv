package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.BodyMetricDto;
import com.healthrecord.healthrecord.entity.BodyMetric;
import java.util.List;
import java.util.Optional;

public interface BodyMetricService {

    Optional<BodyMetric> findLatestBodyMetricByProfileId(Long profileId);

    List<BodyMetric> findAllBodyMetricsByProfileId(Long profileId);

    void saveOrUpdateBodyMetric(BodyMetricDto bodyMetricDto);

    Optional<BodyMetric> findBodyMetricById(Long id);

    void deleteBodyMetricById(Long id);
}
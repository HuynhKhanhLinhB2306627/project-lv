package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.InsuranceCardDto;
import com.healthrecord.healthrecord.entity.InsuranceCard;
import java.util.List;

public interface InsuranceCardService {

    List<InsuranceCard> findCardsByProfileId(Long profileId);

    void saveCard(InsuranceCardDto cardDto);

    InsuranceCard findCardById(Long id);

    void deleteCardById(Long id);

    long countCardsByProfileId(Long profileId);
}
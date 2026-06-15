package com.roy.morago.repository.call;

import com.roy.morago.entity.call.Call;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository extends JpaRepository<Call, Long> {
    boolean existsByIdAndClientId(Long callId, Long clientId);
    boolean existsByIdAndTranslatorId(Long callId, Long translatorId);
}

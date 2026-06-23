package com.roy.morago.repository.call;

import com.roy.morago.entity.call.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CallRepository extends JpaRepository<Call, Long>, JpaSpecificationExecutor<Call> {
    boolean existsByIdAndClientId(Long callId, Long clientId);
    boolean existsByIdAndTranslatorId(Long callId, Long translatorId);

    @Query("SELECT c FROM Call c WHERE c.client.id = :userId OR c.translator.id = :userId")
    Page<Call> findByClientIdOrTranslatorId(@Param("userId") Long userId, Pageable pageable);
}

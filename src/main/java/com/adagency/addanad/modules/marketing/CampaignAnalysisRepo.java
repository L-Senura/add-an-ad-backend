package com.adagency.addanad.modules.marketing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Campaign Analysis and Marketing metrics.
 */
@Repository
public interface CampaignAnalysisRepo extends JpaRepository<CampaignAnalysisDB, Long> {

    /**
     * Find all marketing analysis records linked to a specific client.
     */
    List<CampaignAnalysisDB> findByClientId(Long clientId);

    /**
     * Find marketing analysis for a specific campaign.
     */
    List<CampaignAnalysisDB> findByCampaignId(Long campaignId);
}

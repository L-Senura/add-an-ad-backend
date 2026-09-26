package com.adagency.addanad.modules.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for CampaignDB CRUD and query operations.
 */
@Repository
public interface CampaignRepo extends JpaRepository<CampaignDB, Long> {

    /**
     * Find all campaigns belonging to a specific client.
     */
    List<CampaignDB> findByClientID(Long clientID);

    /**
     * Find campaigns by their category/type (e.g. "In-Site Ad Hype", "Social Media Campaign").
     */
    List<CampaignDB> findByCampaignType(String campaignType);

    /**
     * Find campaigns by status (e.g. "ACTIVE", "COMPLETED").
     */
    List<CampaignDB> findByStatus(String status);
}

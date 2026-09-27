package com.adagency.addanad.modules.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for InvoiceDB operations in the Finance module.
 */
@Repository
public interface InvoiceRepo extends JpaRepository<InvoiceDB, Long> {

    /**
     * Find all invoices billed to a specific client.
     */
    List<InvoiceDB> findByClientId(Long clientId);

    /**
     * Find all invoices associated with a specific campaign.
     */
    List<InvoiceDB> findByCampaignId(Long campaignId);

    /**
     * Find invoices by charged category ("Platform Charges", "Campaign Charges").
     */
    List<InvoiceDB> findByChargedCategory(String chargedCategory);

    /**
     * Find invoices by payment status ("PAID", "PENDING").
     */
    List<InvoiceDB> findByPaymentStatus(String paymentStatus);
}

package com.adagency.addanad.modules.finance;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an Invoice / Payment record in the Finance section.
 * Stores web agency platform charges and campaign charges in the database,
 * enabling financial tracking and report generation.
 */
@Entity
@Table(name = "InvoiceDB")
public class InvoiceDB {

    // Primary Key: innvoice_id auto-incremented
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "innvoice_id")
    private Long invoiceId;

    // Unique ID of the client billed for the invoice
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // Client company name for clear business identification
    @Column(name = "company_name")
    private String companyName;

    // Optional reference to the specific campaign this charge originates from
    @Column(name = "campaign_id")
    private Long campaignId;

    // Charge category: "Platform Charges" or "Campaign Charges"
    @Column(name = "charged_category", nullable = false)
    private String chargedCategory;

    // Price / cost for this specific charge category
    @Column(name = "category_price", nullable = false)
    private Double categoryPrice;

    // Date and time when the payment was completed (null until paid)
    @Column(name = "payed_datetime")
    private LocalDateTime payedDatetime;

    // Remarks or notes sent by client regarding this payment
    @Column(name = "client_description", columnDefinition = "TEXT")
    private String clientDescription;

    // Payment status: "PENDING", "PAID", "CANCELLED"
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "PENDING";

    // Date when the invoice was generated
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public InvoiceDB() {
    }

    public InvoiceDB(Long clientId, Long campaignId, String chargedCategory, Double categoryPrice, String clientDescription) {
        this.clientId = clientId;
        this.campaignId = campaignId;
        this.chargedCategory = chargedCategory;
        this.categoryPrice = categoryPrice;
        this.clientDescription = clientDescription;
        this.paymentStatus = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public InvoiceDB(Long clientId, String companyName, Long campaignId, String chargedCategory, Double categoryPrice, String clientDescription) {
        this.clientId = clientId;
        this.companyName = companyName;
        this.campaignId = campaignId;
        this.chargedCategory = chargedCategory;
        this.categoryPrice = categoryPrice;
        this.clientDescription = clientDescription;
        this.paymentStatus = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getChargedCategory() {
        return chargedCategory;
    }

    public void setChargedCategory(String chargedCategory) {
        this.chargedCategory = chargedCategory;
    }

    public Double getCategoryPrice() {
        return categoryPrice;
    }

    public void setCategoryPrice(Double categoryPrice) {
        this.categoryPrice = categoryPrice;
    }

    public LocalDateTime getPayedDatetime() {
        return payedDatetime;
    }

    public void setPayedDatetime(LocalDateTime payedDatetime) {
        this.payedDatetime = payedDatetime;
    }

    public String getClientDescription() {
        return clientDescription;
    }

    public void setClientDescription(String clientDescription) {
        this.clientDescription = clientDescription;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

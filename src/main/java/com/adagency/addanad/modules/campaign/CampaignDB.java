package com.adagency.addanad.modules.campaign;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an Advertising Campaign.
 * Registered or logged in clients can create campaigns and select campaign types
 * (such as in-site ad hype, social media campaign) and target channels.
 */
@Entity
@Table(name = "CampaignDB")
public class CampaignDB {

    // Primary Key: campaign_id auto-incremented
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    // Foreign Key: ID of the client owning this campaign
    @Column(name = "clientID", nullable = false)
    private Long clientID;

    // Name or title of the campaign (e.g. "Summer Sale 2026")
    @Column(name = "campaign_name")
    private String campaignName;

    // Campaign category/type (e.g. "In-Site Ad Hype", "Social Media Campaign", "Cross-Platform")
    @Column(name = "campaign_type", nullable = false)
    private String campaignType;

    // Comma-separated list of selected ad channels (e.g. "on-site pin, YouTube, FaceBook")
    @Column(name = "selected_channels")
    private String selectedChannels;

    // Total calculated price of the selected campaign channels (e.g. 1000 + 1000 + 500 = 2500)
    @Column(name = "campaign_prices", nullable = false)
    private Double campaignPrices;

    // Current campaign status: "CREATED", "ACTIVE", "COMPLETED", "CANCELLED"
    @Column(name = "status")
    private String status = "ACTIVE";

    // Timestamp when the campaign was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor required by JPA
    public CampaignDB() {
    }

    public CampaignDB(Long clientID, String campaignName, String campaignType, String selectedChannels, Double campaignPrices) {
        this.clientID = clientID;
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.selectedChannels = selectedChannels;
        this.campaignPrices = campaignPrices;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // Automatically assign created_at timestamp before database insertion
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(String campaignType) {
        this.campaignType = campaignType;
    }

    public String getSelectedChannels() {
        return selectedChannels;
    }

    public void setSelectedChannels(String selectedChannels) {
        this.selectedChannels = selectedChannels;
    }

    public Double getCampaignPrices() {
        return campaignPrices;
    }

    public void setCampaignPrices(Double campaignPrices) {
        this.campaignPrices = campaignPrices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

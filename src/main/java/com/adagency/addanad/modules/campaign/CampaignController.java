package com.adagency.addanad.modules.campaign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Managing Advertising Campaigns.
 * Provides APIs for clients to explore campaign types, calculate pricing based on
 * selected channels, and create/manage campaigns.
 */
@RestController
@RequestMapping("/api/campaign")
public class CampaignController {

    @Autowired
    private CampaignRepo campaignRepo;

    @Autowired
    private CampaignPricingService pricingService;

    /**
     * Get the available campaign types and advertising channel rate card.
     */
    @GetMapping("/pricing-catalog")
    public ResponseEntity<?> getPricingCatalog() {
        Map<String, Object> catalog = Map.of(
                "campaignTypes", pricingService.getCampaignTypes(),
                "channelRates", pricingService.getRateCard(),
                "exampleCalculation", "on-site pin (Rs.1000) + YouTube (Rs.1000) + FaceBook (Rs.500) = Rs.2500"
        );
        return ResponseEntity.ok(catalog);
    }

    /**
     * Calculate price breakdown for a selected list of channels before campaign creation.
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<?> calculatePrice(@RequestBody List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Please provide at least one channel to calculate price.");
        }
        Map<String, Object> breakdown = pricingService.getPriceBreakdown(channels);
        return ResponseEntity.ok(breakdown);
    }

    /**
     * Create a new campaign for a client.
     * Automatically computes campaign_prices from selected_channels if not explicitly provided.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCampaign(@RequestBody CampaignDB campaign) {
        // Validation
        if (campaign.getClientID() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Client ID (clientID) is required.");
        }
        if (campaign.getCampaignType() == null || campaign.getCampaignType().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Campaign type is required (e.g. 'In-Site Ad Hype', 'Social Media Campaign').");
        }

        // Calculate campaign prices if missing or zero
        if (campaign.getCampaignPrices() == null || campaign.getCampaignPrices() <= 0) {
            if (campaign.getSelectedChannels() != null && !campaign.getSelectedChannels().trim().isEmpty()) {
                Double computed = pricingService.calculateTotalPriceFromString(campaign.getSelectedChannels());
                campaign.setCampaignPrices(computed);
            } else {
                campaign.setCampaignPrices(0.0);
            }
        }

        if (campaign.getStatus() == null || campaign.getStatus().trim().isEmpty()) {
            campaign.setStatus("ACTIVE");
        }

        CampaignDB saved = campaignRepo.save(campaign);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get all campaigns created by a specific client.
     */
    @GetMapping("/client/{clientId}")
    public List<CampaignDB> getCampaignsByClientId(@PathVariable Long clientId) {
        return campaignRepo.findByClientID(clientId);
    }

    /**
     * Get campaign details by campaign ID.
     */
    @GetMapping("/{campaignId}")
    public ResponseEntity<?> getCampaignById(@PathVariable Long campaignId) {
        Optional<CampaignDB> campaign = campaignRepo.findById(campaignId);
        if (campaign.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign with ID " + campaignId + " not found.");
        }
        return ResponseEntity.ok(campaign.get());
    }

    /**
     * Get all campaigns across the platform (useful for Admins & Marketing Analysts).
     */
    @GetMapping("/all")
    public List<CampaignDB> getAllCampaigns() {
        return campaignRepo.findAll();
    }

    /**
     * Update an existing campaign.
     */
    @PutMapping("/{campaignId}")
    public ResponseEntity<?> updateCampaign(
            @PathVariable Long campaignId,
            @RequestBody CampaignDB updatedData) {

        Optional<CampaignDB> campaignOptional = campaignRepo.findById(campaignId);
        if (campaignOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign with ID " + campaignId + " not found.");
        }

        CampaignDB campaign = campaignOptional.get();

        if (updatedData.getCampaignName() != null) {
            campaign.setCampaignName(updatedData.getCampaignName());
        }
        if (updatedData.getCampaignType() != null) {
            campaign.setCampaignType(updatedData.getCampaignType());
        }
        if (updatedData.getSelectedChannels() != null) {
            campaign.setSelectedChannels(updatedData.getSelectedChannels());
            // Recalculate price if channels changed and new price not explicitly forced
            if (updatedData.getCampaignPrices() == null) {
                campaign.setCampaignPrices(pricingService.calculateTotalPriceFromString(updatedData.getSelectedChannels()));
            }
        }
        if (updatedData.getCampaignPrices() != null) {
            campaign.setCampaignPrices(updatedData.getCampaignPrices());
        }
        if (updatedData.getStatus() != null) {
            campaign.setStatus(updatedData.getStatus());
        }

        CampaignDB saved = campaignRepo.save(campaign);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete a campaign by ID.
     */
    @DeleteMapping("/{campaignId}")
    public ResponseEntity<String> deleteCampaign(@PathVariable Long campaignId) {
        Optional<CampaignDB> campaignOptional = campaignRepo.findById(campaignId);
        if (campaignOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign with ID " + campaignId + " not found.");
        }

        campaignRepo.delete(campaignOptional.get());
        return ResponseEntity.ok("Campaign with ID " + campaignId + " deleted successfully.");
    }
}

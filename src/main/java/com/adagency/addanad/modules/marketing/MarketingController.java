package com.adagency.addanad.modules.marketing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for the Marketing module.
 * Displays campaign analytics, tracks audience views and clicks automatically,
 * and maintains client-campaign-analysis connections.
 */
@RestController
@RequestMapping("/api/marketing")
@CrossOrigin(origins = "*")
public class MarketingController {

    @Autowired
    private CampaignAnalysisRepo campaignAnalysisRepo;

    /**
     * Create a new campaign analysis record for a client.
     * Connects client_id with analysis_id and campaign_id.
     * Views and clicks automatically default to 0 for authentic tracking.
     */
    @PostMapping("/analysis/create")
    public ResponseEntity<?> createAnalysis(@RequestBody CampaignAnalysisDB analysis) {
        if (analysis.getClientId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("client_id is required to connect client with analysis.");
        }

        // Initialize views and clicks to 0 if not provided
        if (analysis.getCampaignViews() == null) {
            analysis.setCampaignViews(0L);
        }
        if (analysis.getClicks() == null) {
            analysis.setClicks(0L);
        }

        CampaignAnalysisDB saved = campaignAnalysisRepo.save(analysis);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Retrieve all campaign analysis records allocated for a specific client.
     */
    @GetMapping("/analysis/client/{clientId}")
    public ResponseEntity<?> getAnalysisByClientId(@PathVariable Long clientId) {
        List<CampaignAnalysisDB> clientAnalysisList = campaignAnalysisRepo.findByClientId(clientId);
        return ResponseEntity.ok(clientAnalysisList);
    }

    /**
     * Retrieve campaign analysis for a specific campaign.
     */
    @GetMapping("/analysis/campaign/{campaignId}")
    public ResponseEntity<?> getAnalysisByCampaignId(@PathVariable Long campaignId) {
        List<CampaignAnalysisDB> analysisList = campaignAnalysisRepo.findByCampaignId(campaignId);
        return ResponseEntity.ok(analysisList);
    }

    /**
     * Retrieve a specific analysis record by its primary key (analysis_id).
     */
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<?> getAnalysisById(@PathVariable Long analysisId) {
        Optional<CampaignAnalysisDB> analysisOptional = campaignAnalysisRepo.findById(analysisId);
        if (analysisOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign analysis with ID " + analysisId + " not found.");
        }
        return ResponseEntity.ok(analysisOptional.get());
    }

    /**
     * Retrieve all campaign analysis records across the platform.
     */
    @GetMapping("/analysis/all")
    public List<CampaignAnalysisDB> getAllAnalyses() {
        return campaignAnalysisRepo.findAll();
    }

    /**
     * Update campaign dates, progress status, or analyst remarks.
     */
    @PutMapping("/analysis/{analysisId}")
    public ResponseEntity<?> updateAnalysis(
            @PathVariable Long analysisId,
            @RequestBody CampaignAnalysisDB updatedData) {

        Optional<CampaignAnalysisDB> analysisOptional = campaignAnalysisRepo.findById(analysisId);
        if (analysisOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign analysis with ID " + analysisId + " not found.");
        }

        CampaignAnalysisDB existing = analysisOptional.get();

        if (updatedData.getCampaignName() != null) {
            existing.setCampaignName(updatedData.getCampaignName());
        }
        if (updatedData.getCampaignViews() != null) {
            existing.setCampaignViews(updatedData.getCampaignViews());
        }
        if (updatedData.getClicks() != null) {
            existing.setClicks(updatedData.getClicks());
        }
        if (updatedData.getVisibleStartDate() != null) {
            existing.setVisibleStartDate(updatedData.getVisibleStartDate());
        }
        if (updatedData.getVisibleEndDate() != null) {
            existing.setVisibleEndDate(updatedData.getVisibleEndDate());
        }
        if (updatedData.getCampaignProgress() != null) {
            existing.setCampaignProgress(updatedData.getCampaignProgress());
        }
        if (updatedData.getRemarks() != null) {
            existing.setRemarks(updatedData.getRemarks());
        }

        CampaignAnalysisDB saved = campaignAnalysisRepo.save(existing);
        return ResponseEntity.ok(saved);
    }

    /**
     * Increment campaign views count by 1 (or custom count) by analysisId.
     */
    @PutMapping("/analysis/{analysisId}/increment-views")
    public ResponseEntity<?> incrementViews(
            @PathVariable Long analysisId,
            @RequestParam(required = false, defaultValue = "1") Long count) {

        Optional<CampaignAnalysisDB> analysisOptional = campaignAnalysisRepo.findById(analysisId);
        if (analysisOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign analysis with ID " + analysisId + " not found.");
        }

        CampaignAnalysisDB analysis = analysisOptional.get();
        long currentViews = analysis.getCampaignViews() != null ? analysis.getCampaignViews() : 0L;
        analysis.setCampaignViews(currentViews + (count != null ? count : 1L));

        CampaignAnalysisDB saved = campaignAnalysisRepo.save(analysis);
        return ResponseEntity.ok(Map.of(
                "analysisId", saved.getAnalysisId(),
                "campaignName", saved.getCampaignName(),
                "updatedCampaignViews", saved.getCampaignViews()
        ));
    }

    /**
     * Increment campaign clicks count by 1 (or custom count) by analysisId.
     * Automatically triggered when an external user clicks on a campaign.
     */
    @PutMapping("/analysis/{analysisId}/increment-clicks")
    public ResponseEntity<?> incrementClicks(
            @PathVariable Long analysisId,
            @RequestParam(required = false, defaultValue = "1") Long count) {

        Optional<CampaignAnalysisDB> analysisOptional = campaignAnalysisRepo.findById(analysisId);
        if (analysisOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign analysis with ID " + analysisId + " not found.");
        }

        CampaignAnalysisDB analysis = analysisOptional.get();
        long currentClicks = analysis.getClicks() != null ? analysis.getClicks() : 0L;
        analysis.setClicks(currentClicks + (count != null ? count : 1L));

        CampaignAnalysisDB saved = campaignAnalysisRepo.save(analysis);
        return ResponseEntity.ok(Map.of(
                "analysisId", saved.getAnalysisId(),
                "campaignName", saved.getCampaignName(),
                "updatedClicks", saved.getClicks()
        ));
    }

    /**
     * Increment views by campaignId.
     * Triggered when an external user loads or views a campaign in the marketplace.
     */
    @PutMapping("/analysis/campaign/{campaignId}/increment-views")
    public ResponseEntity<?> incrementViewsByCampaignId(
            @PathVariable Long campaignId,
            @RequestParam(required = false, defaultValue = "1") Long count) {

        List<CampaignAnalysisDB> analysisList = campaignAnalysisRepo.findByCampaignId(campaignId);
        if (analysisList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No campaign analysis found for campaign ID " + campaignId);
        }

        long incrementBy = (count != null ? count : 1L);
        for (CampaignAnalysisDB analysis : analysisList) {
            long currentViews = analysis.getCampaignViews() != null ? analysis.getCampaignViews() : 0L;
            analysis.setCampaignViews(currentViews + incrementBy);
            campaignAnalysisRepo.save(analysis);
        }

        return ResponseEntity.ok(Map.of(
                "campaignId", campaignId,
                "message", "Campaign views incremented successfully.",
                "updatedRecords", analysisList.size()
        ));
    }

    /**
     * Increment clicks by campaignId.
     * Triggered when an external user clicks on a campaign in the marketplace.
     */
    @PutMapping("/analysis/campaign/{campaignId}/increment-clicks")
    public ResponseEntity<?> incrementClicksByCampaignId(
            @PathVariable Long campaignId,
            @RequestParam(required = false, defaultValue = "1") Long count) {

        List<CampaignAnalysisDB> analysisList = campaignAnalysisRepo.findByCampaignId(campaignId);
        if (analysisList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No campaign analysis found for campaign ID " + campaignId);
        }

        long incrementBy = (count != null ? count : 1L);
        for (CampaignAnalysisDB analysis : analysisList) {
            long currentClicks = analysis.getClicks() != null ? analysis.getClicks() : 0L;
            analysis.setClicks(currentClicks + incrementBy);
            campaignAnalysisRepo.save(analysis);
        }

        return ResponseEntity.ok(Map.of(
                "campaignId", campaignId,
                "message", "Campaign clicks incremented successfully.",
                "updatedRecords", analysisList.size()
        ));
    }

    /**
     * Unified external visitor click handler:
     * Increments both Views and Clicks atomically when an external user clicks on a campaign.
     */
    @PutMapping("/analysis/campaign/{campaignId}/click")
    public ResponseEntity<?> recordCampaignClick(@PathVariable Long campaignId) {
        List<CampaignAnalysisDB> analysisList = campaignAnalysisRepo.findByCampaignId(campaignId);
        if (analysisList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No campaign analysis found for campaign ID " + campaignId);
        }

        for (CampaignAnalysisDB analysis : analysisList) {
            long views = analysis.getCampaignViews() != null ? analysis.getCampaignViews() : 0L;
            long clicks = analysis.getClicks() != null ? analysis.getClicks() : 0L;
            analysis.setCampaignViews(views + 1L);
            analysis.setClicks(clicks + 1L);
            campaignAnalysisRepo.save(analysis);
        }

        return ResponseEntity.ok(Map.of(
                "campaignId", campaignId,
                "message", "Campaign view and click recorded successfully in database."
        ));
    }

    /**
     * Delete an analysis record.
     */
    @DeleteMapping("/analysis/{analysisId}")
    public ResponseEntity<String> deleteAnalysis(@PathVariable Long analysisId) {
        Optional<CampaignAnalysisDB> analysisOptional = campaignAnalysisRepo.findById(analysisId);
        if (analysisOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign analysis with ID " + analysisId + " not found.");
        }

        campaignAnalysisRepo.delete(analysisOptional.get());
        return ResponseEntity.ok("Campaign analysis with ID " + analysisId + " deleted successfully.");
    }
}

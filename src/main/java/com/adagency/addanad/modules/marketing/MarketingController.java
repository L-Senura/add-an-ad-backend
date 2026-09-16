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
 * Displays campaign progress, tracks audience views, visible dates,
 * and maintains client-analysis connections.
 */
@RestController
@RequestMapping("/api/marketing")
public class MarketingController {

    @Autowired
    private CampaignAnalysisRepo campaignAnalysisRepo;

    /**
     * Create a new campaign analysis record for a client.
     * Connects client_id with analysis_id to track campaign views, visible dates, and progress.
     */
    @PostMapping("/analysis/create")
    public ResponseEntity<?> createAnalysis(@RequestBody CampaignAnalysisDB analysis) {
        if (analysis.getClientId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("client_id is required to connect client with analysis.");
        }

        if (analysis.getCampaignViews() == null) {
            analysis.setCampaignViews(0L);
        }

        CampaignAnalysisDB saved = campaignAnalysisRepo.save(analysis);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Retrieve all campaign analysis and progress records allocated for a specific client.
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
     * Accessible by Marketing Analysts and Admins.
     */
    @GetMapping("/analysis/all")
    public List<CampaignAnalysisDB> getAllAnalyses() {
        return campaignAnalysisRepo.findAll();
    }

    /**
     * Update campaign progress, views, visible dates, or analyst remarks.
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
     * Increment campaign views count by 1 (or custom amount), simulating live ad views.
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

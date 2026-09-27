package com.adagency.addanad.modules.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * ReviewController: REST Controller managing advertising agency reviews.
 *
 * Caters to two key workflows:
 * 1. Client-to-Admin Reviews: Brand companies submit confidential reviews to agency
 *    administrators regarding their completed work/campaigns. These reviews are private
 *    and visible exclusively to administrators.
 * 2. Public-to-Client Reviews: Outside visitors/consumers searching for client brands
 *    can read and submit reviews regarding client brands' works and products without
 *    needing to create an account, empowering prospective clients to evaluate brand quality.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepo reviewRepo;

    // =========================================================================
    // SECTION 1: CLIENT -> ADMIN REVIEWS (Confidential / Admin-Only Visibility)
    // =========================================================================

    /**
     * Client submits a review to the agency administrators after a project/work is completed.
     * Accessible via:
     * - POST /api/reviews/client-to-admin
     * - POST /api/reviews/client/{clientId}/to-admin
     *
     * Note: reviewType is set to CLIENT_TO_ADMIN and isPublic is strictly set to false
     * so that this review is only visible to agency administrators.
     */
    @PostMapping({"/client-to-admin", "/client/{clientId}/to-admin"})
    public ResponseEntity<?> submitClientToAdminReview(
            @PathVariable(required = false) Long clientId,
            @RequestBody ReviewDB review) {

        Long effectiveClientId = clientId != null ? clientId : review.getClientID();
        if (effectiveClientId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Client ID is required to submit a review to administrators.");
        }

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Rating is required and must be between 1 and 5 stars.");
        }

        if (review.getReviewMessage() == null || review.getReviewMessage().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Review message cannot be empty.");
        }

        review.setClientID(effectiveClientId);
        review.setReviewType(ReviewDB.TYPE_CLIENT_TO_ADMIN);
        review.setIsPublic(false); // Confidential: Only administrators can view

        if (review.getReviewerName() == null || review.getReviewerName().trim().isEmpty()) {
            review.setReviewerName(review.getClientName() != null ? review.getClientName() : "Client Representative");
        }

        ReviewDB saved = reviewRepo.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Administrators view all confidential client-to-admin reviews.
     * Only returns reviews where reviewType = CLIENT_TO_ADMIN.
     */
    @GetMapping("/admin/all")
    public List<ReviewDB> getAllClientToAdminReviews() {
        return reviewRepo.findByReviewTypeOrderByReviewTimeDesc(ReviewDB.TYPE_CLIENT_TO_ADMIN);
    }

    /**
     * Administrators view client reviews targeted to a specific staff/admin member.
     */
    @GetMapping("/admin/target/{adminId}")
    public List<ReviewDB> getClientReviewsForAdmin(@PathVariable Long adminId) {
        return reviewRepo.findByAdminIDAndReviewTypeOrderByReviewTimeDesc(adminId, ReviewDB.TYPE_CLIENT_TO_ADMIN);
    }

    /**
     * A client views all reviews that they have submitted to agency administrators.
     */
    @GetMapping("/client/{clientId}/sent")
    public List<ReviewDB> getReviewsSentByClient(@PathVariable Long clientId) {
        return reviewRepo.findByClientIDAndReviewTypeOrderByReviewTimeDesc(clientId, ReviewDB.TYPE_CLIENT_TO_ADMIN);
    }

    /**
     * A client updates their previously submitted review to administrators.
     */
    @PutMapping("/client/{clientId}/update/{reviewId}")
    public ResponseEntity<?> updateClientToAdminReview(
            @PathVariable Long clientId,
            @PathVariable Long reviewId,
            @RequestBody ReviewDB updatedData) {

        Optional<ReviewDB> optionalReview = reviewRepo.findById(reviewId);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Review with ID " + reviewId + " not found.");
        }

        ReviewDB existing = optionalReview.get();

        // Enforce ownership: Client can only modify their own review sent to admins
        if (!existing.getClientID().equals(clientId) ||
                !ReviewDB.TYPE_CLIENT_TO_ADMIN.equalsIgnoreCase(existing.getReviewType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own submitted reviews to administrators.");
        }

        if (updatedData.getRating() != null) {
            if (updatedData.getRating() < 1 || updatedData.getRating() > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Rating must be between 1 and 5 stars.");
            }
            existing.setRating(updatedData.getRating());
        }

        if (updatedData.getReviewTitle() != null) {
            existing.setReviewTitle(updatedData.getReviewTitle());
        }

        if (updatedData.getReviewMessage() != null && !updatedData.getReviewMessage().trim().isEmpty()) {
            existing.setReviewMessage(updatedData.getReviewMessage());
        }

        if (updatedData.getWorkReference() != null) {
            existing.setWorkReference(updatedData.getWorkReference());
        }

        if (updatedData.getAdminID() != null) {
            existing.setAdminID(updatedData.getAdminID());
        }

        ReviewDB saved = reviewRepo.save(existing);
        return ResponseEntity.ok(saved);
    }

    /**
     * A client deletes their previously submitted review to administrators.
     */
    @DeleteMapping("/client/{clientId}/delete/{reviewId}")
    public ResponseEntity<String> deleteClientToAdminReview(
            @PathVariable Long clientId,
            @PathVariable Long reviewId) {

        Optional<ReviewDB> optionalReview = reviewRepo.findById(reviewId);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Review with ID " + reviewId + " not found.");
        }

        ReviewDB existing = optionalReview.get();

        if (!existing.getClientID().equals(clientId) ||
                !ReviewDB.TYPE_CLIENT_TO_ADMIN.equalsIgnoreCase(existing.getReviewType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own submitted reviews to administrators.");
        }

        reviewRepo.delete(existing);
        return ResponseEntity.ok("Review with ID " + reviewId + " deleted successfully.");
    }

    // =========================================================================
    // SECTION 2: OUTSIDE PEOPLE -> CLIENT BRAND REVIEWS (Public & No Account Needed)
    // =========================================================================

    /**
     * Outside people / visitors submit a review about a client brand's work/products.
     * NO account or authentication required.
     *
     * Note: reviewType is set to PUBLIC_TO_CLIENT and isPublic is set to true
     * so that other visitors searching for the brand can see it and get an idea of the brand.
     */
    @PostMapping("/public/client/{clientId}")
    public ResponseEntity<?> submitPublicReviewForClient(
            @PathVariable Long clientId,
            @RequestBody ReviewDB review) {

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Rating is required and must be between 1 and 5 stars.");
        }

        if (review.getReviewMessage() == null || review.getReviewMessage().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Review message cannot be empty.");
        }

        review.setClientID(clientId);
        review.setReviewType(ReviewDB.TYPE_PUBLIC_TO_CLIENT);
        review.setIsPublic(true); // Publicly visible for prospective customers and visitors

        // Default reviewer name for outside visitors without an account
        if (review.getReviewerName() == null || review.getReviewerName().trim().isEmpty()) {
            review.setReviewerName("Anonymous Visitor");
        }

        ReviewDB saved = reviewRepo.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Outside visitors (or anyone) view all public reviews for a specific client brand.
     */
    @GetMapping("/public/client/{clientId}")
    public List<ReviewDB> getPublicReviewsForClient(@PathVariable Long clientId) {
        return reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(clientId);
    }

    /**
     * Outside visitors get a comprehensive reputation and rating summary of a client brand.
     * Provides average rating, total review count, star breakdown, and recent reviews.
     */
    @GetMapping("/public/client/{clientId}/summary")
    public ResponseEntity<?> getClientReviewSummary(@PathVariable Long clientId) {
        List<ReviewDB> publicReviews = reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(clientId);

        if (publicReviews.isEmpty()) {
            Map<Integer, Long> emptyBreakdown = new LinkedHashMap<>();
            for (int i = 5; i >= 1; i--) {
                emptyBreakdown.put(i, 0L);
            }
            ReviewSummaryDTO emptySummary = new ReviewSummaryDTO(clientId, null, 0.0, 0L, emptyBreakdown, Collections.emptyList());
            return ResponseEntity.ok(emptySummary);
        }

        String brandName = publicReviews.get(0).getClientName();
        long totalReviews = publicReviews.size();

        Map<Integer, Long> breakdown = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            breakdown.put(i, 0L);
        }

        double sum = 0.0;
        for (ReviewDB r : publicReviews) {
            int rating = r.getRating() != null ? r.getRating() : 0;
            sum += rating;
            if (rating >= 1 && rating <= 5) {
                breakdown.put(rating, breakdown.get(rating) + 1L);
            }
        }

        double rawAverage = sum / totalReviews;
        double roundedAverage = BigDecimal.valueOf(rawAverage)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        ReviewSummaryDTO summary = new ReviewSummaryDTO(
                clientId,
                brandName,
                roundedAverage,
                totalReviews,
                breakdown,
                publicReviews
        );

        return ResponseEntity.ok(summary);
    }

    /**
     * Outside people search for client brand reviews by brand name.
     * E.g. /api/reviews/public/search?name=Nike
     */
    @GetMapping("/public/search")
    public List<ReviewDB> searchPublicReviewsByBrandName(
            @RequestParam(required = false, defaultValue = "") String name) {
        if (name == null || name.trim().isEmpty()) {
            return reviewRepo.findByIsPublicTrueOrderByReviewTimeDesc();
        }
        return reviewRepo.findByClientNameContainingIgnoreCaseAndIsPublicTrueOrderByReviewTimeDesc(name.trim());
    }

    /**
     * Outside visitors browse all public reviews across all client brand companies.
     */
    @GetMapping("/public/all")
    public List<ReviewDB> getAllPublicReviews() {
        return reviewRepo.findByIsPublicTrueOrderByReviewTimeDesc();
    }

    /**
     * A client brand company views all public reviews left by outside visitors regarding their works.
     */
    @GetMapping("/client/{clientId}/received")
    public List<ReviewDB> getReviewsReceivedByClient(@PathVariable Long clientId) {
        return reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(clientId);
    }

    // =========================================================================
    // SECTION 3: ADMIN MANAGEMENT, MODERATION & OVERVIEW
    // =========================================================================

    /**
     * Administrators view overall review metrics across the entire agency platform.
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<AgencyReviewStatsDTO> getAgencyReviewStats() {
        Double avgRating = reviewRepo.findAverageRatingForAgency();
        double formattedAvg = 0.0;
        if (avgRating != null) {
            formattedAvg = BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        long clientToAdminCount = reviewRepo.countByReviewType(ReviewDB.TYPE_CLIENT_TO_ADMIN);
        long publicToClientCount = reviewRepo.countByReviewType(ReviewDB.TYPE_PUBLIC_TO_CLIENT);

        AgencyReviewStatsDTO stats = new AgencyReviewStatsDTO(
                formattedAvg,
                clientToAdminCount,
                publicToClientCount
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Administrators delete/moderate any inappropriate or spam review.
     */
    @DeleteMapping("/admin/delete/{reviewId}")
    public ResponseEntity<String> adminDeleteReview(@PathVariable Long reviewId) {
        Optional<ReviewDB> optionalReview = reviewRepo.findById(reviewId);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Review with ID " + reviewId + " not found.");
        }

        reviewRepo.delete(optionalReview.get());
        return ResponseEntity.ok("Review with ID " + reviewId + " deleted successfully by administrator.");
    }

    /**
     * Retrieve any single review record by ID.
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        Optional<ReviewDB> optionalReview = reviewRepo.findById(reviewId);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Review with ID " + reviewId + " not found.");
        }
        return ResponseEntity.ok(optionalReview.get());
    }
}

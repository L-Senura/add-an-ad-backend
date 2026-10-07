package com.adagency.addanad.modules.communication;

import com.adagency.addanad.modules.communication.observer.CommunicationSubjectImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired(required = false)
    private CommunicationSubjectImpl communicationSubject;

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
        review.setIsPublic(false);

        if (review.getReviewerName() == null || review.getReviewerName().trim().isEmpty()) {
            review.setReviewerName(review.getClientName() != null ? review.getClientName() : "Client Representative");
        }

        ReviewDB saved = reviewRepo.save(review);

        if (communicationSubject != null) {
            communicationSubject.dispatchClientToAdminReviewEvent(
                    saved.getClientID(),
                    saved.getClientName(),
                    saved.getRating(),
                    saved.getReviewTitle(),
                    saved.getReviewID()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/admin/all")
    public List<ReviewDB> getAllClientToAdminReviews() {
        return reviewRepo.findByReviewTypeOrderByReviewTimeDesc(ReviewDB.TYPE_CLIENT_TO_ADMIN);
    }

    @GetMapping("/admin/target/{adminId}")
    public List<ReviewDB> getClientReviewsForAdmin(@PathVariable Long adminId) {
        return reviewRepo.findByAdminIDAndReviewTypeOrderByReviewTimeDesc(adminId, ReviewDB.TYPE_CLIENT_TO_ADMIN);
    }

    @GetMapping("/client/{clientId}/sent")
    public List<ReviewDB> getReviewsSentByClient(@PathVariable Long clientId) {
        return reviewRepo.findByClientIDAndReviewTypeOrderByReviewTimeDesc(clientId, ReviewDB.TYPE_CLIENT_TO_ADMIN);
    }

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
        review.setIsPublic(true);

        if (review.getReviewerName() == null || review.getReviewerName().trim().isEmpty()) {
            review.setReviewerName("Anonymous Visitor");
        }

        ReviewDB saved = reviewRepo.save(review);

        if (communicationSubject != null) {
            communicationSubject.dispatchPublicReviewEvent(
                    saved.getClientID(),
                    saved.getClientName(),
                    saved.getReviewerName(),
                    saved.getRating(),
                    saved.getReviewTitle(),
                    saved.getReviewID()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/public/client/{clientId}")
    public List<ReviewDB> getPublicReviewsForClient(@PathVariable Long clientId) {
        return reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(clientId);
    }

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

    @GetMapping("/public/search")
    public List<ReviewDB> searchPublicReviewsByBrandName(
            @RequestParam(required = false, defaultValue = "") String name) {
        if (name == null || name.trim().isEmpty()) {
            return reviewRepo.findByIsPublicTrueOrderByReviewTimeDesc();
        }
        return reviewRepo.findByClientNameContainingIgnoreCaseAndIsPublicTrueOrderByReviewTimeDesc(name.trim());
    }

    @GetMapping("/public/all")
    public List<ReviewDB> getAllPublicReviews() {
        return reviewRepo.findByIsPublicTrueOrderByReviewTimeDesc();
    }

    @GetMapping("/client/{clientId}/received")
    public List<ReviewDB> getReviewsReceivedByClient(@PathVariable Long clientId) {
        return reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(clientId);
    }

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

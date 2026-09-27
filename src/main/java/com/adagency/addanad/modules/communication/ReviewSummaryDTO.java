package com.adagency.addanad.modules.communication;

import java.util.List;
import java.util.Map;

/**
 * ReviewSummaryDTO: Data Transfer Object providing an aggregated reputation summary
 * for a client brand company.
 *
 * Allows outside people who are searching for client brands to quickly understand
 * the client company's overall rating, star breakdown, and view recent public reviews.
 */
public class ReviewSummaryDTO {

    private Long clientID;
    private String clientName;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingBreakdown;
    private List<ReviewDB> recentReviews;

    public ReviewSummaryDTO() {
    }

    public ReviewSummaryDTO(Long clientID, String clientName, Double averageRating,
                            Long totalReviews, Map<Integer, Long> ratingBreakdown,
                            List<ReviewDB> recentReviews) {
        this.clientID = clientID;
        this.clientName = clientName;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.ratingBreakdown = ratingBreakdown;
        this.recentReviews = recentReviews;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Map<Integer, Long> getRatingBreakdown() {
        return ratingBreakdown;
    }

    public void setRatingBreakdown(Map<Integer, Long> ratingBreakdown) {
        this.ratingBreakdown = ratingBreakdown;
    }

    public List<ReviewDB> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<ReviewDB> recentReviews) {
        this.recentReviews = recentReviews;
    }
}

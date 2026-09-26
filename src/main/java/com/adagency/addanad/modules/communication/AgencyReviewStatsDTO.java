package com.adagency.addanad.modules.communication;

/**
 * AgencyReviewStatsDTO: Data Transfer Object providing agency administrators
 * with high-level performance insights, including agency rating from clients
 * and platform review volumes.
 */
public class AgencyReviewStatsDTO {

    private Double agencyAverageRating;
    private Long totalClientToAdminReviews;
    private Long totalPublicToClientReviews;

    public AgencyReviewStatsDTO() {
    }

    public AgencyReviewStatsDTO(Double agencyAverageRating, Long totalClientToAdminReviews, Long totalPublicToClientReviews) {
        this.agencyAverageRating = agencyAverageRating;
        this.totalClientToAdminReviews = totalClientToAdminReviews;
        this.totalPublicToClientReviews = totalPublicToClientReviews;
    }

    public Double getAgencyAverageRating() {
        return agencyAverageRating;
    }

    public void setAgencyAverageRating(Double agencyAverageRating) {
        this.agencyAverageRating = agencyAverageRating;
    }

    public Long getTotalClientToAdminReviews() {
        return totalClientToAdminReviews;
    }

    public void setTotalClientToAdminReviews(Long totalClientToAdminReviews) {
        this.totalClientToAdminReviews = totalClientToAdminReviews;
    }

    public Long getTotalPublicToClientReviews() {
        return totalPublicToClientReviews;
    }

    public void setTotalPublicToClientReviews(Long totalPublicToClientReviews) {
        this.totalPublicToClientReviews = totalPublicToClientReviews;
    }
}

package com.adagency.addanad.modules.communication;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ReviewDB: JPA entity representing reviews in the advertising agency platform.
 *
 * Supports two essential review workflows:
 * 1. CLIENT_TO_ADMIN: Submitted by clients (brand companies) to agency administrators
 *    after successful completion of advertising work. These reviews are confidential
 *    and visible exclusively to administrators.
 * 2. PUBLIC_TO_CLIENT: Submitted by outside people (visitors/consumers) searching for
 *    client brand companies to review their works and products. Outside people do NOT
 *    need to create an account to leave or read reviews, allowing prospective customers
 *    to easily learn about the brand company.
 */
@Entity
@Table(name = "ReviewDB")
public class ReviewDB {

    // Predefined constants for review types
    public static final String TYPE_CLIENT_TO_ADMIN = "CLIENT_TO_ADMIN";
    public static final String TYPE_PUBLIC_TO_CLIENT = "PUBLIC_TO_CLIENT";

    // Primary Key for ReviewDB
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewID")
    private Long reviewID;

    // Review classification: "CLIENT_TO_ADMIN" or "PUBLIC_TO_CLIENT"
    @Column(name = "reviewType", nullable = false)
    private String reviewType;

    // For CLIENT_TO_ADMIN: ID of the client submitting the review
    // For PUBLIC_TO_CLIENT: ID of the client (brand company) being evaluated
    @Column(name = "clientID", nullable = false)
    private Long clientID;

    // Brand company name (stored for fast querying, display, and search)
    @Column(name = "clientName")
    private String clientName;

    // Optional admin ID if the client review targets a specific agency staff member
    @Column(name = "adminID")
    private Long adminID;

    // Reviewer name:
    // For PUBLIC_TO_CLIENT: Name entered by the outside visitor (e.g. "Jane Doe" or "Anonymous Visitor")
    // For CLIENT_TO_ADMIN: Client company representative or company name
    @Column(name = "reviewerName")
    private String reviewerName;

    // Optional email of the outside reviewer or client contact
    @Column(name = "reviewerEmail")
    private String reviewerEmail;

    // Numerical rating (1 to 5 stars)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    // Headline or summary of the review (e.g., "Outstanding Billboard Campaign")
    @Column(name = "reviewTitle")
    private String reviewTitle;

    // Detailed feedback text
    @Column(name = "reviewMessage", columnDefinition = "TEXT", nullable = false)
    private String reviewMessage;

    // Reference to the specific campaign, advertisement, or work completed
    @Column(name = "workReference")
    private String workReference;

    // Visibility flag:
    // false = Confidential/Internal to admins (used for CLIENT_TO_ADMIN)
    // true  = Publicly visible to outside visitors (used for PUBLIC_TO_CLIENT)
    @Column(name = "isPublic", nullable = false)
    private Boolean isPublic = false;

    // Timestamp when the review was created
    @Column(name = "reviewTime")
    private LocalDateTime reviewTime;

    // Default no-args constructor required by JPA
    public ReviewDB() {
    }

    // Full constructor
    public ReviewDB(String reviewType, Long clientID, String clientName, Long adminID,
                    String reviewerName, String reviewerEmail, Integer rating,
                    String reviewTitle, String reviewMessage, String workReference,
                    Boolean isPublic) {
        this.reviewType = reviewType;
        this.clientID = clientID;
        this.clientName = clientName;
        this.adminID = adminID;
        this.reviewerName = reviewerName;
        this.reviewerEmail = reviewerEmail;
        this.rating = rating;
        this.reviewTitle = reviewTitle;
        this.reviewMessage = reviewMessage;
        this.workReference = workReference;
        this.isPublic = isPublic != null ? isPublic : TYPE_PUBLIC_TO_CLIENT.equalsIgnoreCase(reviewType);
        this.reviewTime = LocalDateTime.now();
    }

    // Convenience factory method for Client -> Admin reviews (Private to Admins)
    public static ReviewDB createClientToAdminReview(Long clientID, String clientName, Long adminID,
                                                     String reviewerName, Integer rating,
                                                     String reviewTitle, String reviewMessage,
                                                     String workReference) {
        ReviewDB review = new ReviewDB();
        review.setReviewType(TYPE_CLIENT_TO_ADMIN);
        review.setClientID(clientID);
        review.setClientName(clientName);
        review.setAdminID(adminID);
        review.setReviewerName(reviewerName != null && !reviewerName.trim().isEmpty() ? reviewerName : clientName);
        review.setRating(rating);
        review.setReviewTitle(reviewTitle);
        review.setReviewMessage(reviewMessage);
        review.setWorkReference(workReference);
        review.setIsPublic(false); // Only visible to admins
        review.setReviewTime(LocalDateTime.now());
        return review;
    }

    // Convenience factory method for Outside People -> Client Brand reviews (Public, no account needed)
    public static ReviewDB createPublicToClientReview(Long clientID, String clientName,
                                                     String reviewerName, String reviewerEmail,
                                                     Integer rating, String reviewTitle,
                                                     String reviewMessage, String workReference) {
        ReviewDB review = new ReviewDB();
        review.setReviewType(TYPE_PUBLIC_TO_CLIENT);
        review.setClientID(clientID);
        review.setClientName(clientName);
        review.setReviewerName(reviewerName != null && !reviewerName.trim().isEmpty() ? reviewerName : "Anonymous Visitor");
        review.setReviewerEmail(reviewerEmail);
        review.setRating(rating);
        review.setReviewTitle(reviewTitle);
        review.setReviewMessage(reviewMessage);
        review.setWorkReference(workReference);
        review.setIsPublic(true); // Publicly visible so others can get an idea of the brand
        review.setReviewTime(LocalDateTime.now());
        return review;
    }

    // Automatically set timestamp and defaults before persisting
    @PrePersist
    protected void onCreate() {
        if (this.reviewTime == null) {
            this.reviewTime = LocalDateTime.now();
        }
        if (this.isPublic == null) {
            this.isPublic = TYPE_PUBLIC_TO_CLIENT.equalsIgnoreCase(this.reviewType);
        }
        if (this.reviewerName == null || this.reviewerName.trim().isEmpty()) {
            this.reviewerName = TYPE_PUBLIC_TO_CLIENT.equalsIgnoreCase(this.reviewType)
                    ? "Anonymous Visitor" : "Client Representative";
        }
    }

    // Getters and Setters
    public Long getReviewID() {
        return reviewID;
    }

    public void setReviewID(Long reviewID) {
        this.reviewID = reviewID;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
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

    public Long getAdminID() {
        return adminID;
    }

    public void setAdminID(Long adminID) {
        this.adminID = adminID;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public String getWorkReference() {
        return workReference;
    }

    public void setWorkReference(String workReference) {
        this.workReference = workReference;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }
}

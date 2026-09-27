package com.adagency.addanad.modules.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Marks this interface as a Spring Data repository for ReviewDB CRUD operations.
@Repository
public interface ReviewRepo extends JpaRepository<ReviewDB, Long> {

    /**
     * Query to find all reviews associated with a specific client ID.
     *
     * @param clientID The ID of the client
     * @return List of ReviewDB records associated with the given client ID
     */
    List<ReviewDB> findByClientID(Long clientID);

    /**
     * Query to find all reviews associated with a specific client ID, ordered newest first.
     *
     * @param clientID The ID of the client
     * @return List of ReviewDB records in descending chronological order
     */
    List<ReviewDB> findByClientIDOrderByReviewTimeDesc(Long clientID);

    /**
     * Query to retrieve all reviews across the platform, ordered newest first.
     *
     * @return List of all ReviewDB records in descending chronological order
     */
    List<ReviewDB> findAllByOrderByReviewTimeDesc();

    /**
     * Query to find all reviews by their review type (e.g. CLIENT_TO_ADMIN or PUBLIC_TO_CLIENT).
     *
     * @param reviewType Type of review
     * @return List of reviews of the specified type ordered newest first
     */
    List<ReviewDB> findByReviewTypeOrderByReviewTimeDesc(String reviewType);

    /**
     * Query to find reviews for a given client filtered by review type.
     *
     * @param clientID The client's ID
     * @param reviewType The review type (CLIENT_TO_ADMIN or PUBLIC_TO_CLIENT)
     * @return List of matching reviews ordered newest first
     */
    List<ReviewDB> findByClientIDAndReviewTypeOrderByReviewTimeDesc(Long clientID, String reviewType);

    /**
     * Query to find reviews targeted to a specific agency admin by admin ID.
     *
     * @param adminID The administrator's ID
     * @param reviewType The review type (typically CLIENT_TO_ADMIN)
     * @return List of reviews targeted to the admin ordered newest first
     */
    List<ReviewDB> findByAdminIDAndReviewTypeOrderByReviewTimeDesc(Long adminID, String reviewType);

    /**
     * Query to find all public reviews for a specific client brand company.
     *
     * @param clientID The ID of the client brand company
     * @return List of public reviews for outside people to view
     */
    List<ReviewDB> findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(Long clientID);

    /**
     * Query to find all public reviews across all client brands, ordered newest first.
     *
     * @return List of all public reviews
     */
    List<ReviewDB> findByIsPublicTrueOrderByReviewTimeDesc();

    /**
     * Search public reviews by client or brand name (case-insensitive) for outside visitors.
     *
     * @param clientName Part or all of the brand name
     * @return List of matching public reviews
     */
    List<ReviewDB> findByClientNameContainingIgnoreCaseAndIsPublicTrueOrderByReviewTimeDesc(String clientName);

    /**
     * Count total public reviews for a specific client brand.
     *
     * @param clientID Client ID
     * @return Number of public reviews
     */
    long countByClientIDAndIsPublicTrue(Long clientID);

    /**
     * Count total reviews of a specific type (e.g., CLIENT_TO_ADMIN or PUBLIC_TO_CLIENT).
     *
     * @param reviewType Review type
     * @return Count of reviews
     */
    long countByReviewType(String reviewType);

    /**
     * Calculate average rating for a specific client brand based on public reviews.
     *
     * @param clientID Client ID
     * @return Average rating or null if no reviews exist
     */
    @Query("SELECT AVG(r.rating) FROM ReviewDB r WHERE r.clientID = :clientID AND r.isPublic = true")
    Double findAverageRatingByClientID(@Param("clientID") Long clientID);

    /**
     * Calculate overall average rating given by clients to the agency admins.
     *
     * @return Average rating for agency
     */
    @Query("SELECT AVG(r.rating) FROM ReviewDB r WHERE r.reviewType = 'CLIENT_TO_ADMIN'")
    Double findAverageRatingForAgency();
}

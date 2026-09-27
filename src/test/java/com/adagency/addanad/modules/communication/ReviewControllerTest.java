package com.adagency.addanad.modules.communication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewRepo reviewRepo;

    @InjectMocks
    private ReviewController reviewController;

    private ReviewDB clientToAdminReview;
    private ReviewDB publicToClientReview;

    @BeforeEach
    void setUp() {
        clientToAdminReview = new ReviewDB();
        clientToAdminReview.setReviewID(1L);
        clientToAdminReview.setReviewType(ReviewDB.TYPE_CLIENT_TO_ADMIN);
        clientToAdminReview.setClientID(10L);
        clientToAdminReview.setClientName("Apex Apparel");
        clientToAdminReview.setAdminID(2L);
        clientToAdminReview.setReviewerName("Marketing Director");
        clientToAdminReview.setRating(5);
        clientToAdminReview.setReviewTitle("Outstanding Billboard Campaign");
        clientToAdminReview.setReviewMessage("Agency delivered exceptional results on time!");
        clientToAdminReview.setWorkReference("Summer 2026 Campaign");
        clientToAdminReview.setIsPublic(false);
        clientToAdminReview.setReviewTime(LocalDateTime.now());

        publicToClientReview = new ReviewDB();
        publicToClientReview.setReviewID(2L);
        publicToClientReview.setReviewType(ReviewDB.TYPE_PUBLIC_TO_CLIENT);
        publicToClientReview.setClientID(10L);
        publicToClientReview.setClientName("Apex Apparel");
        publicToClientReview.setReviewerName("Jane Consumer");
        publicToClientReview.setReviewerEmail("jane@example.com");
        publicToClientReview.setRating(4);
        publicToClientReview.setReviewTitle("Loved the new summer collection");
        publicToClientReview.setReviewMessage("Saw their ads and bought two jackets. Great quality!");
        publicToClientReview.setWorkReference("Summer Jacket Line");
        publicToClientReview.setIsPublic(true);
        publicToClientReview.setReviewTime(LocalDateTime.now());
    }

    // =========================================================================
    // CLIENT -> ADMIN REVIEW TESTS (Confidential / Admin-Only)
    // =========================================================================

    @Test
    void testSubmitClientToAdminReview_Success() {
        ReviewDB input = new ReviewDB();
        input.setClientID(10L);
        input.setClientName("Apex Apparel");
        input.setAdminID(2L);
        input.setRating(5);
        input.setReviewTitle("Top agency work");
        input.setReviewMessage("Super creative ideas for our brand");
        input.setWorkReference("Q3 Digital Campaign");

        when(reviewRepo.save(any(ReviewDB.class))).thenAnswer(invocation -> {
            ReviewDB r = invocation.getArgument(0);
            r.setReviewID(100L);
            return r;
        });

        ResponseEntity<?> response = reviewController.submitClientToAdminReview(10L, input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        ReviewDB saved = (ReviewDB) response.getBody();
        assertEquals(ReviewDB.TYPE_CLIENT_TO_ADMIN, saved.getReviewType());
        assertFalse(saved.getIsPublic(), "Client to admin review must NOT be public");
        assertEquals(10L, saved.getClientID());
        assertEquals(5, saved.getRating());
    }

    @Test
    void testSubmitClientToAdminReview_InvalidRating_ReturnsBadRequest() {
        ReviewDB input = new ReviewDB();
        input.setClientID(10L);
        input.setRating(6); // Invalid rating > 5
        input.setReviewMessage("Good");

        ResponseEntity<?> response = reviewController.submitClientToAdminReview(10L, input);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSubmitClientToAdminReview_EmptyMessage_ReturnsBadRequest() {
        ReviewDB input = new ReviewDB();
        input.setClientID(10L);
        input.setRating(4);
        input.setReviewMessage("   "); // Blank message

        ResponseEntity<?> response = reviewController.submitClientToAdminReview(10L, input);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetAllClientToAdminReviews_OnlyReturnsAdminReviews() {
        when(reviewRepo.findByReviewTypeOrderByReviewTimeDesc(ReviewDB.TYPE_CLIENT_TO_ADMIN))
                .thenReturn(List.of(clientToAdminReview));

        List<ReviewDB> results = reviewController.getAllClientToAdminReviews();

        assertEquals(1, results.size());
        assertEquals(ReviewDB.TYPE_CLIENT_TO_ADMIN, results.get(0).getReviewType());
        assertFalse(results.get(0).getIsPublic());
    }

    @Test
    void testGetClientReviewsForAdmin() {
        when(reviewRepo.findByAdminIDAndReviewTypeOrderByReviewTimeDesc(2L, ReviewDB.TYPE_CLIENT_TO_ADMIN))
                .thenReturn(List.of(clientToAdminReview));

        List<ReviewDB> results = reviewController.getClientReviewsForAdmin(2L);
        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).getAdminID());
    }

    @Test
    void testGetReviewsSentByClient() {
        when(reviewRepo.findByClientIDAndReviewTypeOrderByReviewTimeDesc(10L, ReviewDB.TYPE_CLIENT_TO_ADMIN))
                .thenReturn(List.of(clientToAdminReview));

        List<ReviewDB> results = reviewController.getReviewsSentByClient(10L);
        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).getClientID());
    }

    @Test
    void testUpdateClientToAdminReview_Success() {
        when(reviewRepo.findById(1L)).thenReturn(Optional.of(clientToAdminReview));
        when(reviewRepo.save(any(ReviewDB.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDB updatedData = new ReviewDB();
        updatedData.setRating(4);
        updatedData.setReviewMessage("Updated review message");

        ResponseEntity<?> response = reviewController.updateClientToAdminReview(10L, 1L, updatedData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewDB saved = (ReviewDB) response.getBody();
        assertNotNull(saved);
        assertEquals(4, saved.getRating());
        assertEquals("Updated review message", saved.getReviewMessage());
    }

    @Test
    void testUpdateClientToAdminReview_ForbiddenWhenOtherClientTries() {
        when(reviewRepo.findById(1L)).thenReturn(Optional.of(clientToAdminReview));

        ReviewDB updatedData = new ReviewDB();
        updatedData.setRating(4);

        // Client 999 tries to update review belonging to Client 10
        ResponseEntity<?> response = reviewController.updateClientToAdminReview(999L, 1L, updatedData);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteClientToAdminReview_Success() {
        when(reviewRepo.findById(1L)).thenReturn(Optional.of(clientToAdminReview));
        doNothing().when(reviewRepo).delete(clientToAdminReview);

        ResponseEntity<String> response = reviewController.deleteClientToAdminReview(10L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewRepo, times(1)).delete(clientToAdminReview);
    }

    // =========================================================================
    // OUTSIDE PEOPLE -> CLIENT BRAND REVIEW TESTS (Public / No Account)
    // =========================================================================

    @Test
    void testSubmitPublicReviewForClient_SuccessWithoutAccount() {
        ReviewDB input = new ReviewDB();
        input.setRating(5);
        input.setReviewTitle("Impressive campaign");
        input.setReviewMessage("I noticed their promotional billboard in the city, really creative!");
        // Notice: No reviewerName provided, testing automatic fallback to Anonymous Visitor
        input.setReviewerName("");

        when(reviewRepo.save(any(ReviewDB.class))).thenAnswer(invocation -> {
            ReviewDB r = invocation.getArgument(0);
            r.setReviewID(200L);
            return r;
        });

        ResponseEntity<?> response = reviewController.submitPublicReviewForClient(10L, input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ReviewDB saved = (ReviewDB) response.getBody();
        assertNotNull(saved);
        assertEquals(ReviewDB.TYPE_PUBLIC_TO_CLIENT, saved.getReviewType());
        assertTrue(saved.getIsPublic(), "Public review must be visible to everyone");
        assertEquals(10L, saved.getClientID());
        assertEquals("Anonymous Visitor", saved.getReviewerName());
    }

    @Test
    void testGetPublicReviewsForClient() {
        when(reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(10L))
                .thenReturn(List.of(publicToClientReview));

        List<ReviewDB> results = reviewController.getPublicReviewsForClient(10L);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsPublic());
    }

    @Test
    void testGetClientReviewSummary_ProvidesRatingBreakdown() {
        ReviewDB r1 = new ReviewDB();
        r1.setClientID(10L);
        r1.setClientName("Apex Apparel");
        r1.setRating(5);
        r1.setIsPublic(true);

        ReviewDB r2 = new ReviewDB();
        r2.setClientID(10L);
        r2.setClientName("Apex Apparel");
        r2.setRating(4);
        r2.setIsPublic(true);

        when(reviewRepo.findByClientIDAndIsPublicTrueOrderByReviewTimeDesc(10L))
                .thenReturn(List.of(r1, r2));

        ResponseEntity<?> response = reviewController.getClientReviewSummary(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ReviewSummaryDTO summary = (ReviewSummaryDTO) response.getBody();
        assertNotNull(summary);
        assertEquals(10L, summary.getClientID());
        assertEquals("Apex Apparel", summary.getClientName());
        assertEquals(2L, summary.getTotalReviews());
        assertEquals(4.5, summary.getAverageRating());
        assertEquals(1L, summary.getRatingBreakdown().get(5));
        assertEquals(1L, summary.getRatingBreakdown().get(4));
        assertEquals(0L, summary.getRatingBreakdown().get(3));
    }

    @Test
    void testSearchPublicReviewsByBrandName() {
        when(reviewRepo.findByClientNameContainingIgnoreCaseAndIsPublicTrueOrderByReviewTimeDesc("Apex"))
                .thenReturn(List.of(publicToClientReview));

        List<ReviewDB> results = reviewController.searchPublicReviewsByBrandName("Apex");
        assertEquals(1, results.size());
        assertEquals("Apex Apparel", results.get(0).getClientName());
    }

    // =========================================================================
    // ADMIN OVERVIEW & MODERATION TESTS
    // =========================================================================

    @Test
    void testGetAgencyReviewStats() {
        when(reviewRepo.findAverageRatingForAgency()).thenReturn(4.75);
        when(reviewRepo.countByReviewType(ReviewDB.TYPE_CLIENT_TO_ADMIN)).thenReturn(12L);
        when(reviewRepo.countByReviewType(ReviewDB.TYPE_PUBLIC_TO_CLIENT)).thenReturn(34L);

        ResponseEntity<AgencyReviewStatsDTO> response = reviewController.getAgencyReviewStats();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AgencyReviewStatsDTO stats = response.getBody();
        assertNotNull(stats);
        assertEquals(4.8, stats.getAgencyAverageRating()); // Rounded to 1 decimal place
        assertEquals(12L, stats.getTotalClientToAdminReviews());
        assertEquals(34L, stats.getTotalPublicToClientReviews());
    }

    @Test
    void testAdminDeleteReview_Moderation() {
        when(reviewRepo.findById(2L)).thenReturn(Optional.of(publicToClientReview));
        doNothing().when(reviewRepo).delete(publicToClientReview);

        ResponseEntity<String> response = reviewController.adminDeleteReview(2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewRepo, times(1)).delete(publicToClientReview);
    }
}

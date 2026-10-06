package com.adagency.addanad.modules.finance;

import com.adagency.addanad.modules.campaign.CampaignDB;
import com.adagency.addanad.modules.campaign.CampaignRepo;
import com.adagency.addanad.modules.client.ClientDB;
import com.adagency.addanad.modules.client.ClientRepo;
import com.adagency.addanad.modules.finance.dto.FinanceReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST Controller for Finance and Invoicing.
 * Manages web agency platform charges and campaign charges,
 * processes payment tracking with client remarks, supports full invoice lifecycle
 * (creation, retrieval, update, deletion), and generates financial reports.
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @Autowired
    private InvoiceRepo invoiceRepo;

    @Autowired
    private CampaignRepo campaignRepo;

    @Autowired
    private ClientRepo clientRepo;

    // Standard default platform charge fee
    private static final double DEFAULT_PLATFORM_CHARGE = 500.0;

    /**
     * Helper to backfill/enrich company name if missing from an invoice record
     */
    private void enrichInvoiceWithCompanyName(InvoiceDB invoice) {
        if (invoice != null && (invoice.getCompanyName() == null || invoice.getCompanyName().trim().isEmpty())) {
            if (invoice.getClientId() != null) {
                clientRepo.findById(invoice.getClientId()).ifPresent(client -> {
                    if (client.getCompanyName() != null && !client.getCompanyName().trim().isEmpty()) {
                        invoice.setCompanyName(client.getCompanyName());
                    }
                });
            }
        }
    }

    /**
     * Automatically generate platform charges and campaign charges for an existing campaign.
     * Creates two separate invoice entries:
     * 1) "Campaign Charges" reflecting the selected channels total
     * 2) "Platform Charges" reflecting agency web platform hosting/service fee
     */
    @PostMapping("/invoice/generate/{campaignId}")
    public ResponseEntity<?> generateCampaignInvoices(
            @PathVariable Long campaignId,
            @RequestParam(required = false, defaultValue = "500.0") Double platformCharge) {

        Optional<CampaignDB> campaignOptional = campaignRepo.findById(campaignId);
        if (campaignOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Campaign with ID " + campaignId + " not found.");
        }

        CampaignDB campaign = campaignOptional.get();

        // Resolve client company name
        String clientCompany = null;
        Optional<ClientDB> clientOptional = clientRepo.findById(campaign.getClientID());
        if (clientOptional.isPresent() && clientOptional.get().getCompanyName() != null) {
            clientCompany = clientOptional.get().getCompanyName();
        }

        // 1. Campaign Charges Invoice
        InvoiceDB campaignInvoice = new InvoiceDB();
        campaignInvoice.setClientId(campaign.getClientID());
        campaignInvoice.setCompanyName(clientCompany);
        campaignInvoice.setCampaignId(campaign.getCampaignId());
        campaignInvoice.setChargedCategory("Campaign Charges");
        campaignInvoice.setCategoryPrice(campaign.getCampaignPrices());
        campaignInvoice.setPaymentStatus("PENDING");
        campaignInvoice.setClientDescription("Charges for campaign: " + campaign.getCampaignName() + " (" + campaign.getSelectedChannels() + ")");
        InvoiceDB savedCampaignInvoice = invoiceRepo.save(campaignInvoice);

        // 2. Platform Charges Invoice
        InvoiceDB platformInvoice = new InvoiceDB();
        platformInvoice.setClientId(campaign.getClientID());
        platformInvoice.setCompanyName(clientCompany);
        platformInvoice.setCampaignId(campaign.getCampaignId());
        platformInvoice.setChargedCategory("Platform Charges");
        platformInvoice.setCategoryPrice(platformCharge != null ? platformCharge : DEFAULT_PLATFORM_CHARGE);
        platformInvoice.setPaymentStatus("PENDING");
        platformInvoice.setClientDescription("Web agency platform service fee for campaign #" + campaign.getCampaignId());
        InvoiceDB savedPlatformInvoice = invoiceRepo.save(platformInvoice);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Invoices generated successfully for campaign #" + campaignId,
                "campaignChargesInvoice", savedCampaignInvoice,
                "platformChargesInvoice", savedPlatformInvoice,
                "totalAmount", savedCampaignInvoice.getCategoryPrice() + savedPlatformInvoice.getCategoryPrice()
        ));
    }

    /**
     * Manually create an invoice entry (e.g. custom platform charges or adjustments).
     */
    @PostMapping("/invoice/create")
    public ResponseEntity<?> createInvoice(@RequestBody InvoiceDB invoice) {
        if (invoice.getClientId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("client_id is required.");
        }
        if (invoice.getChargedCategory() == null || invoice.getChargedCategory().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("charged_category is required ('Platform Charges' or 'Campaign Charges').");
        }
        if (invoice.getCategoryPrice() == null || invoice.getCategoryPrice() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("category_price must be a valid positive number.");
        }

        if (invoice.getPaymentStatus() == null || invoice.getPaymentStatus().trim().isEmpty()) {
            invoice.setPaymentStatus("PENDING");
        }

        // Automatically associate company name if omitted
        if (invoice.getCompanyName() == null || invoice.getCompanyName().trim().isEmpty()) {
            clientRepo.findById(invoice.getClientId()).ifPresent(client -> {
                if (client.getCompanyName() != null && !client.getCompanyName().trim().isEmpty()) {
                    invoice.setCompanyName(client.getCompanyName());
                }
            });
        }

        InvoiceDB saved = invoiceRepo.save(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update an existing invoice entry (category, price, status, notes).
     */
    @PutMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long invoiceId, @RequestBody InvoiceDB updatedInvoice) {
        Optional<InvoiceDB> invoiceOptional = invoiceRepo.findById(invoiceId);
        if (invoiceOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Invoice with ID " + invoiceId + " not found."));
        }

        InvoiceDB invoice = invoiceOptional.get();
        if (updatedInvoice.getClientId() != null) invoice.setClientId(updatedInvoice.getClientId());
        if (updatedInvoice.getCompanyName() != null) invoice.setCompanyName(updatedInvoice.getCompanyName());
        if (updatedInvoice.getCampaignId() != null) invoice.setCampaignId(updatedInvoice.getCampaignId());
        if (updatedInvoice.getChargedCategory() != null) invoice.setChargedCategory(updatedInvoice.getChargedCategory());
        if (updatedInvoice.getCategoryPrice() != null) invoice.setCategoryPrice(updatedInvoice.getCategoryPrice());
        if (updatedInvoice.getPaymentStatus() != null) invoice.setPaymentStatus(updatedInvoice.getPaymentStatus());
        if (updatedInvoice.getClientDescription() != null) invoice.setClientDescription(updatedInvoice.getClientDescription());
        if (updatedInvoice.getPayedDatetime() != null) invoice.setPayedDatetime(updatedInvoice.getPayedDatetime());

        enrichInvoiceWithCompanyName(invoice);
        InvoiceDB saved = invoiceRepo.save(invoice);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete an invoice entry by its ID.
     */
    @DeleteMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long invoiceId) {
        Optional<InvoiceDB> invoiceOptional = invoiceRepo.findById(invoiceId);
        if (invoiceOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Invoice with ID " + invoiceId + " not found."));
        }

        invoiceRepo.deleteById(invoiceId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invoice #" + invoiceId + " deleted successfully."
        ));
    }

    /**
     * Mark an invoice as paid, recording the payment datetime and client description/remarks.
     */
    @PutMapping("/invoice/{invoiceId}/pay")
    public ResponseEntity<?> recordPayment(
            @PathVariable Long invoiceId,
            @RequestBody(required = false) Map<String, String> paymentDetails) {

        Optional<InvoiceDB> invoiceOptional = invoiceRepo.findById(invoiceId);
        if (invoiceOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invoice with ID " + invoiceId + " not found.");
        }

        InvoiceDB invoice = invoiceOptional.get();
        invoice.setPaymentStatus("PAID");
        invoice.setPayedDatetime(LocalDateTime.now());

        if (paymentDetails != null && paymentDetails.containsKey("client_description")) {
            invoice.setClientDescription(paymentDetails.get("client_description"));
        }

        enrichInvoiceWithCompanyName(invoice);
        InvoiceDB saved = invoiceRepo.save(invoice);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment recorded successfully for invoice #" + invoiceId,
                "invoice", saved
        ));
    }

    /**
     * Get all invoices with optional filter by category or status.
     */
    @GetMapping("/invoices")
    public List<InvoiceDB> getAllInvoices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        List<InvoiceDB> list;
        if (category != null && !category.trim().isEmpty()) {
            list = invoiceRepo.findByChargedCategory(category.trim());
        } else if (status != null && !status.trim().isEmpty()) {
            list = invoiceRepo.findByPaymentStatus(status.trim().toUpperCase());
        } else {
            list = invoiceRepo.findAll();
        }

        list.forEach(this::enrichInvoiceWithCompanyName);
        return list;
    }

    /**
     * Get all invoices for a specific client.
     */
    @GetMapping("/invoices/client/{clientId}")
    public List<InvoiceDB> getInvoicesByClientId(@PathVariable Long clientId) {
        List<InvoiceDB> list = invoiceRepo.findByClientId(clientId);
        list.forEach(this::enrichInvoiceWithCompanyName);
        return list;
    }

    /**
     * Get all invoices for a specific campaign.
     */
    @GetMapping("/invoices/campaign/{campaignId}")
    public List<InvoiceDB> getInvoicesByCampaignId(@PathVariable Long campaignId) {
        List<InvoiceDB> list = invoiceRepo.findByCampaignId(campaignId);
        list.forEach(this::enrichInvoiceWithCompanyName);
        return list;
    }

    /**
     * Generate an aggregated Financial Report.
     * Computes total revenues, breakdown between Platform Charges & Campaign Charges,
     * paid vs pending counts, and includes detailed itemized records.
     */
    @GetMapping("/report")
    public ResponseEntity<FinanceReportDTO> generateFinancialReport() {
        List<InvoiceDB> allInvoices = invoiceRepo.findAll();

        FinanceReportDTO report = new FinanceReportDTO();
        report.setTotalInvoices(allInvoices.size());

        double totalBilled = 0.0;
        double totalCollected = 0.0;
        double totalPending = 0.0;
        double totalPlatform = 0.0;
        double totalCampaign = 0.0;
        int paidCount = 0;
        int pendingCount = 0;

        for (InvoiceDB inv : allInvoices) {
            enrichInvoiceWithCompanyName(inv);
            double price = inv.getCategoryPrice() != null ? inv.getCategoryPrice() : 0.0;
            totalBilled += price;

            // Charged category aggregation
            if ("Platform Charges".equalsIgnoreCase(inv.getChargedCategory())) {
                totalPlatform += price;
            } else if ("Campaign Charges".equalsIgnoreCase(inv.getChargedCategory())) {
                totalCampaign += price;
            }

            // Payment status aggregation
            if ("PAID".equalsIgnoreCase(inv.getPaymentStatus())) {
                totalCollected += price;
                paidCount++;
            } else {
                totalPending += price;
                pendingCount++;
            }
        }

        report.setTotalBilledAmount(totalBilled);
        report.setTotalRevenueCollected(totalCollected);
        report.setTotalPendingAmount(totalPending);
        report.setTotalPlatformCharges(totalPlatform);
        report.setTotalCampaignCharges(totalCampaign);
        report.setPaidInvoicesCount(paidCount);
        report.setPendingInvoicesCount(pendingCount);
        report.setInvoiceList(allInvoices);

        return ResponseEntity.ok(report);
    }
}

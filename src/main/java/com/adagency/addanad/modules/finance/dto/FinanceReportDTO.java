package com.adagency.addanad.modules.finance.dto;

import com.adagency.addanad.modules.finance.InvoiceDB;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing an Aggregated Financial Report.
 * Summarizes platform charges, campaign charges, collection metrics, and itemized invoice details.
 */
public class FinanceReportDTO {

    private int totalInvoices;
    private Double totalBilledAmount;
    private Double totalRevenueCollected;
    private Double totalPendingAmount;
    private Double totalPlatformCharges;
    private Double totalCampaignCharges;
    private int paidInvoicesCount;
    private int pendingInvoicesCount;
    private LocalDateTime reportGeneratedTime;
    private List<InvoiceDB> invoiceList;

    public FinanceReportDTO() {
        this.reportGeneratedTime = LocalDateTime.now();
    }

    public int getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(int totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public Double getTotalBilledAmount() {
        return totalBilledAmount;
    }

    public void setTotalBilledAmount(Double totalBilledAmount) {
        this.totalBilledAmount = totalBilledAmount;
    }

    public Double getTotalRevenueCollected() {
        return totalRevenueCollected;
    }

    public void setTotalRevenueCollected(Double totalRevenueCollected) {
        this.totalRevenueCollected = totalRevenueCollected;
    }

    public Double getTotalPendingAmount() {
        return totalPendingAmount;
    }

    public void setTotalPendingAmount(Double totalPendingAmount) {
        this.totalPendingAmount = totalPendingAmount;
    }

    public Double getTotalPlatformCharges() {
        return totalPlatformCharges;
    }

    public void setTotalPlatformCharges(Double totalPlatformCharges) {
        this.totalPlatformCharges = totalPlatformCharges;
    }

    public Double getTotalCampaignCharges() {
        return totalCampaignCharges;
    }

    public void setTotalCampaignCharges(Double totalCampaignCharges) {
        this.totalCampaignCharges = totalCampaignCharges;
    }

    public int getPaidInvoicesCount() {
        return paidInvoicesCount;
    }

    public void setPaidInvoicesCount(int paidInvoicesCount) {
        this.paidInvoicesCount = paidInvoicesCount;
    }

    public int getPendingInvoicesCount() {
        return pendingInvoicesCount;
    }

    public void setPendingInvoicesCount(int pendingInvoicesCount) {
        this.pendingInvoicesCount = pendingInvoicesCount;
    }

    public LocalDateTime getReportGeneratedTime() {
        return reportGeneratedTime;
    }

    public void setReportGeneratedTime(LocalDateTime reportGeneratedTime) {
        this.reportGeneratedTime = reportGeneratedTime;
    }

    public List<InvoiceDB> getInvoiceList() {
        return invoiceList;
    }

    public void setInvoiceList(List<InvoiceDB> invoiceList) {
        this.invoiceList = invoiceList;
    }
}

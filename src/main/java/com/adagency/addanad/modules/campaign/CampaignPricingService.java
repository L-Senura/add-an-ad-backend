package com.adagency.addanad.modules.campaign;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service to manage campaign channels, categories, and calculate campaign pricing.
 * Example rates as defined:
 *  - on-site pin: Rs. 1000
 *  - YouTube: Rs. 1000
 *  - FaceBook: Rs. 500
 *  - Instagram: Rs. 800
 *  - Google Ads: Rs. 1200
 *  - TikTok: Rs. 700
 */
@Service
public class CampaignPricingService {

    // Standard rate card for advertising channels (prices in Rs.)
    private static final Map<String, Double> RATE_CARD = new LinkedHashMap<>();

    // Standard campaign categories/types
    private static final List<String> CAMPAIGN_TYPES = Arrays.asList(
            "In-Site Ad Hype",
            "Social Media Campaign",
            "Video Promo Campaign",
            "Search Engine Hype",
            "Omnichannel Campaign"
    );

    static {
        RATE_CARD.put("on-site pin", 1000.0);
        RATE_CARD.put("YouTube", 1000.0);
        RATE_CARD.put("FaceBook", 500.0);
        RATE_CARD.put("Instagram", 800.0);
        RATE_CARD.put("Google Ads", 1200.0);
        RATE_CARD.put("TikTok", 700.0);
    }

    /**
     * Get the full advertising channel rate card.
     */
    public Map<String, Double> getRateCard() {
        return Collections.unmodifiableMap(RATE_CARD);
    }

    /**
     * Get the list of supported campaign categories.
     */
    public List<String> getCampaignTypes() {
        return Collections.unmodifiableList(CAMPAIGN_TYPES);
    }

    /**
     * Calculate price for a single channel name (case-insensitive match).
     */
    public Double getChannelPrice(String channelName) {
        if (channelName == null) return 0.0;
        String trimmed = channelName.trim();
        for (Map.Entry<String, Double> entry : RATE_CARD.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                return entry.getValue();
            }
        }
        // Fallback default base price if custom channel
        return 500.0;
    }

    /**
     * Calculate total campaign price from a list of channel names.
     */
    public Double calculateTotalPrice(List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (String ch : channels) {
            total += getChannelPrice(ch);
        }
        return total;
    }

    /**
     * Calculate total campaign price from a comma-separated string.
     */
    public Double calculateTotalPriceFromString(String channelsCsv) {
        if (channelsCsv == null || channelsCsv.trim().isEmpty()) {
            return 0.0;
        }
        String[] parts = channelsCsv.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            if (!p.trim().isEmpty()) {
                list.add(p.trim());
            }
        }
        return calculateTotalPrice(list);
    }

    /**
     * Returns an itemized breakdown of channel prices and the total sum.
     */
    public Map<String, Object> getPriceBreakdown(List<String> channels) {
        Map<String, Object> breakdown = new LinkedHashMap<>();
        Map<String, Double> items = new LinkedHashMap<>();
        double total = 0.0;

        if (channels != null) {
            for (String ch : channels) {
                String trimmed = ch.trim();
                double price = getChannelPrice(trimmed);
                items.put(trimmed, price);
                total += price;
            }
        }

        breakdown.put("itemizedPrices", items);
        breakdown.put("totalCampaignPrice", total);
        return breakdown;
    }
}

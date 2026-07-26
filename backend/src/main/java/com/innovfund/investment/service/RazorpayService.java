package com.innovfund.investment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.innovfund.common.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Raw REST against Razorpay's Payment Links API (same manual-JSON-string approach used for Gemini
 * in this codebase, and for the same reason: predictable serialization without relying on a
 * message-converter that Cloudinary's transitive Gson dependency can quietly hijack).
 *
 * Payment confirmation is polled (see PaymentVerificationJob), not webhook-driven — a local dev
 * server has no public URL for Razorpay to call back to.
 */
@Service
@Slf4j
public class RazorpayService {

    private static final String BASE_URL = "https://api.razorpay.com/v1/payment_links";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${RAZORPAY_KEY_ID:}")
    private String keyId;

    @Value("${RAZORPAY_KEY_SECRET:}")
    private String keySecret;

    public record PaymentLink(String id, String shortUrl) {
    }

    private RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15_000);
        factory.setReadTimeout(20_000);
        return RestClient.builder().requestFactory(factory).build();
    }

    private String basicAuthHeader() {
        String credentials = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isConfigured() {
        return !keyId.isBlank() && !keySecret.isBlank();
    }

    public PaymentLink createPaymentLink(BigDecimal amount, String description, String customerName, String customerEmail) {
        if (!isConfigured()) {
            throw new BadRequestException("Razorpay is not configured (RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET)");
        }

        ObjectNode customer = objectMapper.createObjectNode();
        customer.put("name", customerName);
        customer.put("email", customerEmail);

        ObjectNode notify = objectMapper.createObjectNode();
        notify.put("sms", false);
        notify.put("email", false);

        ObjectNode body = objectMapper.createObjectNode();
        // Razorpay wants paise. longValue(), not intValue() — a funding amount past ~₹2.1 crore
        // would silently overflow and wrap to a wrong (often negative) int.
        body.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
        body.put("currency", "INR");
        body.put("description", description);
        body.set("customer", customer);
        body.set("notify", notify);
        body.put("reminder_enable", false);

        String responseBody;
        try {
            responseBody = restClient().post()
                    .uri(BASE_URL)
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(body))
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new BadRequestException("Could not create Razorpay payment link: " + e.getMessage());
        }

        JsonNode parsed;
        try {
            parsed = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new BadRequestException("Razorpay returned a response that could not be parsed");
        }

        return new PaymentLink(parsed.path("id").asText(), parsed.path("short_url").asText());
    }

    /** Returns Razorpay's raw status string: created, partially_paid, paid, cancelled, or expired. */
    public String fetchStatus(String paymentLinkId) {
        if (!isConfigured()) {
            return "created";
        }
        try {
            String responseBody = restClient().get()
                    .uri(BASE_URL + "/" + paymentLinkId)
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(responseBody).path("status").asText();
        } catch (Exception e) {
            log.warn("Could not fetch Razorpay payment link status for {}: {}", paymentLinkId, e.getMessage());
            return "created";
        }
    }
}

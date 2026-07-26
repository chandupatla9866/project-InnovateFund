package com.innovfund.investment.service;

import com.innovfund.investment.entity.Investment;
import com.innovfund.investment.entity.InvestmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Polls Razorpay for pending investments' payment link status instead of relying on a webhook —
 * a local dev backend has no public URL for Razorpay to call back to. Every 2 minutes is a
 * reasonable balance between prompt confirmation and not hammering Razorpay's API.
 */
@Component
@RequiredArgsConstructor
public class PaymentVerificationJob {

    private final InvestmentService investmentService;
    private final RazorpayService razorpayService;

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void verifyPendingPayments() {
        if (!razorpayService.isConfigured()) {
            return;
        }
        List<Investment> pending = investmentService.findAllByStatus(InvestmentStatus.PENDING);
        for (Investment investment : pending) {
            if (investment.getRazorpayPaymentLinkId() == null) {
                continue;
            }
            String status = razorpayService.fetchStatus(investment.getRazorpayPaymentLinkId());
            if ("paid".equals(status)) {
                investmentService.markPaid(investment);
            } else if ("cancelled".equals(status) || "expired".equals(status)) {
                investmentService.markCancelled(investment);
            }
        }
    }
}

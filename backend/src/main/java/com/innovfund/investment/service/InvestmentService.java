package com.innovfund.investment.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.BadRequestException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.investment.dto.InvestmentDto;
import com.innovfund.investment.dto.RecordInvestmentRequest;
import com.innovfund.investment.entity.Investment;
import com.innovfund.investment.entity.InvestmentStatus;
import com.innovfund.investment.repository.InvestmentRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.UserRepository;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final StartupService startupService;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;
    private final RazorpayService razorpayService;

    /**
     * Founder records an investment -> a real Razorpay payment link is generated for the investor
     * to actually pay. Funding progress does NOT move yet; it only moves once
     * PaymentVerificationJob confirms Razorpay reports the link as paid.
     */
    @Transactional
    public InvestmentDto record(User founder, UUID startupId, RecordInvestmentRequest request) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        User investor = userRepository.findById(request.investorId())
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found"));
        if (investor.getRole() != Role.INVESTOR) {
            throw new BadRequestException("Selected user is not an investor");
        }

        RazorpayService.PaymentLink link = razorpayService.createPaymentLink(
                request.amount(), "Investment in " + startup.getName(),
                userDisplayNameService.resolveFullName(investor), investor.getEmail());

        Investment investment = Investment.builder()
                .startup(startup)
                .investor(investor)
                .amount(request.amount())
                .notes(request.notes())
                .status(InvestmentStatus.PENDING)
                .razorpayPaymentLinkId(link.id())
                .razorpayPaymentLinkUrl(link.shortUrl())
                .build();
        investment = investmentRepository.save(investment);

        notificationService.notify(investor, NotificationType.INVESTMENT_RECEIVED,
                userDisplayNameService.resolveFullName(founder) + " recorded a ₹" + request.amount()
                        + " investment for you in " + startup.getName() + " — complete payment from your Portfolio page",
                "/investor/portfolio");

        return toDto(investment);
    }

    /** Called by PaymentVerificationJob once Razorpay confirms the payment link was actually paid. */
    @Transactional
    public void markPaid(Investment investment) {
        if (investment.getStatus() == InvestmentStatus.PAID) {
            return;
        }
        investment.setStatus(InvestmentStatus.PAID);
        investment.setPaidAt(Instant.now());
        investmentRepository.save(investment);

        Startup startup = investment.getStartup();
        startupService.increaseFundingProgress(startup.getFounder(), startup.getId(), investment.getAmount());

        notificationService.notify(investment.getInvestor(), NotificationType.INVESTMENT_RECEIVED,
                "Your ₹" + investment.getAmount() + " payment for " + startup.getName() + " was confirmed",
                "/investor/portfolio");
        notificationService.notify(startup.getFounder(), NotificationType.INVESTMENT_RECEIVED,
                userDisplayNameService.resolveFullName(investment.getInvestor()) + "'s ₹" + investment.getAmount()
                        + " payment for " + startup.getName() + " was confirmed",
                "/startups/" + startup.getId());
    }

    /** Called by PaymentVerificationJob if the investor cancels or lets the payment link expire. */
    @Transactional
    public void markCancelled(Investment investment) {
        if (investment.getStatus() != InvestmentStatus.PENDING) {
            return;
        }
        investment.setStatus(InvestmentStatus.CANCELLED);
        investmentRepository.save(investment);

        notificationService.notify(investment.getStartup().getFounder(), NotificationType.INVESTMENT_RECEIVED,
                userDisplayNameService.resolveFullName(investment.getInvestor()) + "'s ₹" + investment.getAmount()
                        + " payment link for " + investment.getStartup().getName() + " was cancelled or expired",
                "/startups/" + investment.getStartup().getId());
    }

    @Transactional(readOnly = true)
    public List<Investment> findAllByStatus(InvestmentStatus status) {
        return investmentRepository.findAllByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<InvestmentDto> forStartup(User founder, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return investmentRepository.findAllByStartupIdOrderByCreatedAtDesc(startupId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<InvestmentDto> myPortfolio(User investor) {
        return investmentRepository.findAllByInvestorIdOrderByCreatedAtDesc(investor.getId()).stream().map(this::toDto).toList();
    }

    private InvestmentDto toDto(Investment i) {
        return new InvestmentDto(i.getId(), i.getStartup().getId(), i.getStartup().getName(),
                i.getInvestor().getId(), userDisplayNameService.resolveFullName(i.getInvestor()),
                i.getAmount(), i.getNotes(), i.getStatus(),
                i.getStatus() == InvestmentStatus.PENDING ? i.getRazorpayPaymentLinkUrl() : null,
                i.getPaidAt(), i.getCreatedAt());
    }
}

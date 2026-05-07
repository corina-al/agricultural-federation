package edu.hei.school.agricultural.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MemberPayment {
    private String id;
    private String memberId;
    private MembershipFee membershipFee;
    private FinancialAccount accountCredited;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private LocalDate creationDate;
}
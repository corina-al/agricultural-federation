package edu.hei.school.agricultural.controller.dto;

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
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private Object accountCredited;
    private LocalDate creationDate;
}
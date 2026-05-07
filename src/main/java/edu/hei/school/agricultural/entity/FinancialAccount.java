package edu.hei.school.agricultural.entity;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FinancialAccount {
    private String id;
    private String collectivityId;
    private FinancialAccountType accountType;
    private String holderName;
    private MobileBankingService mobileBankingService;
    private String mobileNumber;
    private Bank bankName;
    private Integer bankCode;
    private Integer bankBranchCode;
    private Long bankAccountNumber;
    private Integer bankAccountKey;
    private BigDecimal amount;
}
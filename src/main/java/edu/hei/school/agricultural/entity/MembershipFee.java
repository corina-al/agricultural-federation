package edu.hei.school.agricultural.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MembershipFee {
    private String id;
    private String collectivityId;
    private String label;
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private BigDecimal amount;
    private ActivityStatus status;
}
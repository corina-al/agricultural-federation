package edu.hei.school.agricultural.entity;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CollectivityLocalStatistics {
    private Member member;
    private BigDecimal earnedAmount;
    private BigDecimal unpaidAmount;
    private Double assiduityPercentage;
}
package edu.hei.school.agricultural.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MonthlyRecurrenceRule {
    private Integer weekOrdinal;
    private String dayOfWeek;
}
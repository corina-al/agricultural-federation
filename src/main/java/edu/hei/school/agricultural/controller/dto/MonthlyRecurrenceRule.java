package edu.hei.school.agricultural.controller.dto;

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
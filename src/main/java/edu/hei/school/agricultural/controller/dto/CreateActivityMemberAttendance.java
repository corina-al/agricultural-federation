package edu.hei.school.agricultural.controller.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateActivityMemberAttendance {
    private String memberIdentifier;
    private String attendanceStatus;
}
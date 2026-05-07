package edu.hei.school.agricultural.controller.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ActivityMemberAttendance {
    private String id;
    private MemberDescription memberDescription;
    private String attendanceStatus;
}
package edu.hei.school.agricultural.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ActivityAttendance {
    private String id;
    private String activityId;
    private Member member;
    private AttendanceStatus attendanceStatus;
}
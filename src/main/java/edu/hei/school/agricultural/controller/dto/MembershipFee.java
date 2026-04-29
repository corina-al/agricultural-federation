package edu.hei.school.agricultural.controller.dto;

import edu.hei.school.agricultural.api.model.ActivityStatus;
import edu.hei.school.agricultural.api.model.CreateMembershipFee;

public class MembershipFee extends CreateMembershipFee {

    public String id;
    public ActivityStatus status;
}
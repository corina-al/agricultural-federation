package edu.hei.school.agricultural.controller.dto;

import java.util.List;

public class CreateMemberDto extends MemberInformation {

    public String collectivityIdentifier;
    public List<String> referees;
    public Boolean registrationFeePaid;
    public Boolean membershipDuesPaid;
}
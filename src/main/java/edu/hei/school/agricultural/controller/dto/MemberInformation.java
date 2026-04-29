package edu.hei.school.agricultural.controller.dto;

import edu.hei.school.agricultural.api.model.Gender;
import edu.hei.school.agricultural.api.model.MemberOccupation;

import java.time.LocalDate;

public class MemberInformation {

    public String firstName;
    public String lastName;
    public LocalDate birthDate;
    public Gender gender;
    public String address;
    public String profession;
    public Integer phoneNumber;
    public String email;
    public MemberOccupation occupation;
}
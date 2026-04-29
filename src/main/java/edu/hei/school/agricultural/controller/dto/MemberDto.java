package edu.hei.school.agricultural.controller.dto;

import java.util.List;
import java.util.stream.Collectors;

public class MemberDto extends MemberInformation {

    public String id;
    public List<MemberDto> referees;

    @Override
    public String toString() {
        var refereesIds = "[]";
        if (referees != null) {
            refereesIds = referees.stream().map(memberDto -> memberDto.id).collect(Collectors.joining(","));
        }
        return "Member{" +
                "id='" + id + '\'' +
                ", referees=" + refereesIds +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                ", address='" + address + '\'' +
                ", profession='" + profession + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", email='" + email + '\'' +
                ", occupation=" + occupation +
                '}';
    }
}
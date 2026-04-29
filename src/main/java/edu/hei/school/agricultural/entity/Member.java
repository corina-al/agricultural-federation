package edu.hei.school.agricultural.entity;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Member {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String profession;
    private String phoneNumber;
    private String email;
    private MemberOccupation occupation;
    private List<Member> referees;
    private List<Collectivity> collectivities;
    private Boolean registrationFeePaid;
    private Boolean membershipDuesPaid;

    public List<Collectivity> addCollectivity(Collectivity collectivity) {
        collectivities.add(collectivity);
        collectivity.getMembers().add(this);
        return collectivities;
    }

}

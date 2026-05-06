package edu.hei.school.agricultural.entity;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static edu.hei.school.agricultural.entity.MemberOccupation.JUNIOR;

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

    public boolean refereesAreEligible() {
        if (referees == null || referees.isEmpty()) {
            return false;
        }
        var memberRefereesInsideActualCollectivityNotJuniorCount = referees.stream().filter(member ->
                        member.getCollectivities() != null
                        && member.getCollectivities().stream()
                                .anyMatch(collectivity -> collectivities.contains(collectivity) && !JUNIOR.equals(member.getOccupation())))
                .count();
        return memberRefereesInsideActualCollectivityNotJuniorCount >= 2;
    }

    public List<Collectivity> addCollectivity(Collectivity collectivity) {
        if (collectivities == null) {
            collectivities = new ArrayList<>();
        }
        collectivities.add(collectivity);
        return collectivities;
    }

    public List<Member> addReferees(List<Member> refereeMembers) {
        if (referees == null) {
            referees = new ArrayList<>();
        }

        referees.addAll(refereeMembers);

        return referees;
    }

}

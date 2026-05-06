package edu.hei.school.agricultural.entity;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Collectivity {
    private String id;
    private String name;
    private Integer number;
    private String location;
    private String specialization;
    private CollectivityStructure collectivityStructure;
    private List<Member> members;
    private Boolean federationApproval;

    public boolean hasEnoughMembers() {
        return members.size() >= 10;
    }

    public List<Member> addMembers(List<Member> newMembers) {
        if(members == null){
            members = new ArrayList<>();
        }
        for (Member member : newMembers) {
            member.getCollectivities().add(this);
        }
        members.addAll(newMembers);

        return members;
    }
}

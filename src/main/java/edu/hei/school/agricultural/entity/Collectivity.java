package edu.hei.school.agricultural.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Collectivity {
    private String id;
    private List<Member> members;

    public boolean hasEnoughMembers() {
        return members.size() >= 10;
    }
}

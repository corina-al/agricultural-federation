package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.repository.CollectivityRepository;
import edu.hei.school.agricultural.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CollectivityRepository collectivityRepository;

    public List<Member> addNewMembers(List<Member> memberList) {
        for (Member member : memberList) {
            member.setId(randomUUID().toString());
        }
        memberRepository.saveAll(memberList);
        throw new UnsupportedOperationException("Not implemented");
    }
}

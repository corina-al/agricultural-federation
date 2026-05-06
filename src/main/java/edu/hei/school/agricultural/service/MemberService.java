package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public List<Member> addNewMembers(List<Member> memberList) {
        for (Member member : memberList) {
            if (!member.refereesAreEligible()) {
                throw new BadRequestException("Member.id=" + member.getId() + " member referees are not eligible");
            }
            if (!member.getMembershipDuesPaid()) {
                throw new BadRequestException("Member.id=" + member.getId() + " membership dues not paid");
            }
            if (!member.getRegistrationFeePaid()) {
                throw new BadRequestException("Member.id=" + member.getId() + " membership fees not paid");
            }
            member.setId(randomUUID().toString());
        }
        return memberRepository.saveAll(memberList);
    }
}

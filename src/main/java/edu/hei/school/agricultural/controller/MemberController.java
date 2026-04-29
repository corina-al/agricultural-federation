package edu.hei.school.agricultural.controller;

import edu.hei.school.agricultural.controller.dto.CreateMemberDto;
import edu.hei.school.agricultural.controller.dto.MemberDto;
import edu.hei.school.agricultural.controller.mapper.MemberDtoMapper;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;

    @PostMapping("/members")
    public ResponseEntity<?> createMembers(List<CreateMemberDto> createMemberDtos) {
        try {
            List<Member> convertedCreateMembers = createMemberDtos.stream()
                    .map(createMemberDto -> memberDtoMapper.mapToEntity(createMemberDto))
                    .toList();

            List<Member> savedMembers = memberService.addNewMembers(convertedCreateMembers);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(savedMembers.stream()
                            .map(member -> new MemberDto()) //TODO
                            .toList());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}

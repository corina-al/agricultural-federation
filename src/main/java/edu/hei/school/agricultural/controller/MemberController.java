package edu.hei.school.agricultural.controller;

import edu.hei.school.agricultural.controller.dto.CreateMember;
import edu.hei.school.agricultural.controller.mapper.MemberDtoMapper;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;

    @PostMapping("/members")
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> createMemberDtos) {
        try {
            List<Member> convertedCreateMembers = createMemberDtos.stream()
                    .map(memberDtoMapper::mapToEntity)
                    .toList();

            List<Member> savedMembers = memberService.addNewMembers(convertedCreateMembers);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(savedMembers.stream()
                            .map(memberDtoMapper::mapToDto)
                            .toList());
        } catch (BadRequestException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}

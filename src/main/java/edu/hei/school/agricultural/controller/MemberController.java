package edu.hei.school.agricultural.controller;

import edu.hei.school.agricultural.controller.dto.CreateMember;
import edu.hei.school.agricultural.controller.dto.CreateMemberPayment;
import edu.hei.school.agricultural.controller.mapper.CollectivityDtoMapper;
import edu.hei.school.agricultural.controller.mapper.MemberDtoMapper;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.entity.MemberPayment;
import edu.hei.school.agricultural.entity.FinancialAccount;
import edu.hei.school.agricultural.entity.MembershipFee;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.repository.FinancialAccountRepository;
import edu.hei.school.agricultural.repository.MembershipFeeRepository;
import edu.hei.school.agricultural.service.CollectivityService;
import edu.hei.school.agricultural.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;
    private final CollectivityService collectivityService;
    private final CollectivityDtoMapper collectivityDtoMapper;
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;

    @PostMapping("/members")
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> createMemberDtos) {
        try {
            List<Member> convertedCreateMembers = createMemberDtos.stream()
                    .map(memberDtoMapper::mapToEntity)
                    .toList();
            List<Member> savedMembers = memberService.addNewMembers(convertedCreateMembers);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedMembers.stream()
                            .map(memberDtoMapper::mapToDto)
                            .toList());
        } catch (BadRequestException e) {
            return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/members/{id}/payments")
    public ResponseEntity<?> createPayments(@PathVariable String id, @RequestBody List<CreateMemberPayment> createPaymentDtos) {
        try {
            List<MemberPayment> payments = createPaymentDtos.stream().map(dto -> {
                MembershipFee fee = dto.getMembershipFeeIdentifier() == null ? null :
                        membershipFeeRepository.findById(dto.getMembershipFeeIdentifier())
                                .orElseThrow(() -> new NotFoundException("MembershipFee.id=" + dto.getMembershipFeeIdentifier() + " not found"));
                FinancialAccount account = financialAccountRepository.findById(dto.getAccountCreditedIdentifier())
                        .orElseThrow(() -> new NotFoundException("FinancialAccount.id=" + dto.getAccountCreditedIdentifier() + " not found"));
                return MemberPayment.builder()
                        .amount(dto.getAmount())
                        .paymentMode(dto.getPaymentMode() == null ? null : edu.hei.school.agricultural.entity.PaymentMode.valueOf(dto.getPaymentMode().name()))
                        .membershipFee(fee)
                        .accountCredited(account)
                        .build();
            }).toList();
            List<MemberPayment> saved = collectivityService.createMemberPayments(id, payments);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(saved.stream().map(p -> edu.hei.school.agricultural.controller.dto.MemberPayment.builder()
                            .id(p.getId())
                            .amount(p.getAmount())
                            .paymentMode(p.getPaymentMode() == null ? null : edu.hei.school.agricultural.controller.dto.PaymentMode.valueOf(p.getPaymentMode().name()))
                            .accountCredited(p.getAccountCredited() == null ? null : collectivityDtoMapper.mapFinancialAccountToDto(p.getAccountCredited()))
                            .creationDate(p.getCreationDate())
                            .build()).toList());
        } catch (BadRequestException e) {
            return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
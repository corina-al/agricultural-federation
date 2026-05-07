package edu.hei.school.agricultural.controller.mapper;

import edu.hei.school.agricultural.controller.dto.*;
import edu.hei.school.agricultural.entity.*;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.repository.FinancialAccountRepository;
import edu.hei.school.agricultural.repository.MemberRepository;
import edu.hei.school.agricultural.repository.MembershipFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CollectivityDtoMapper {
    private final MemberRepository memberRepository;
    private final MemberDtoMapper memberDtoMapper;
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;

    public edu.hei.school.agricultural.controller.dto.Collectivity mapToDto(edu.hei.school.agricultural.entity.Collectivity collectivity) {
        CollectivityStructure collectivityStructure = collectivity.getCollectivityStructure();
        return edu.hei.school.agricultural.controller.dto.Collectivity.builder()
                .id(collectivity.getId())
                .name(collectivity.getName())
                .number(collectivity.getNumber())
                .location(collectivity.getLocation())
                .structure(collectivityStructure == null ? null : edu.hei.school.agricultural.controller.dto.CollectivityStructure.builder()
                        .president(memberDtoMapper.mapToDto(collectivityStructure.getPresident()))
                        .vicePresident(memberDtoMapper.mapToDto(collectivityStructure.getVicePresident()))
                        .treasurer(memberDtoMapper.mapToDto(collectivityStructure.getTreasurer()))
                        .secretary(memberDtoMapper.mapToDto(collectivityStructure.getSecretary()))
                        .build())
                .members(collectivity.getMembers().stream()
                        .map(memberDtoMapper::mapToDto)
                        .toList())
                .build();
    }

    public edu.hei.school.agricultural.entity.CollectivityStructure mapToEntity(CreateCollectivityStructure createCollectivityStructure) {
        return edu.hei.school.agricultural.entity.CollectivityStructure.builder()
                .president(memberRepository.findById(createCollectivityStructure.getPresident())
                        .orElseThrow(() -> new NotFoundException("Member.id" + createCollectivityStructure.getPresident() + " not found")))
                .vicePresident(memberRepository.findById(createCollectivityStructure.getVicePresident())
                        .orElseThrow(() -> new NotFoundException("Member.id" + createCollectivityStructure.getVicePresident() + " not found")))
                .treasurer(memberRepository.findById(createCollectivityStructure.getTreasurer())
                        .orElseThrow(() -> new NotFoundException("Member.id" + createCollectivityStructure.getTreasurer() + " not found")))
                .secretary(memberRepository.findById(createCollectivityStructure.getSecretary())
                        .orElseThrow(() -> new NotFoundException("Member.id" + createCollectivityStructure.getSecretary() + " not found")))
                .build();
    }

    public edu.hei.school.agricultural.entity.Collectivity mapToEntity(CreateCollectivity createCollectivity) {
        var createCollectivityStructure = createCollectivity.getStructure();
        return edu.hei.school.agricultural.entity.Collectivity.builder()
                .location(createCollectivity.getLocation())
                .collectivityStructure(mapToEntity(createCollectivityStructure))
                .federationApproval(createCollectivity.getFederationApproval())
                .members(createCollectivity.getMembers().stream()
                        .map(memberIdentifier -> memberRepository.findById(memberIdentifier).orElseThrow(() -> new NotFoundException("Member.id=" + memberIdentifier + " not found")))
                        .toList())
                .build();
    }

    public MembershipFee mapMembershipFeeToDto(edu.hei.school.agricultural.entity.MembershipFee fee) {
        return MembershipFee.builder()
                .id(fee.getId())
                .eligibleFrom(fee.getEligibleFrom())
                .frequency(fee.getFrequency() == null ? null : Frequency.valueOf(fee.getFrequency().name()))
                .amount(fee.getAmount())
                .label(fee.getLabel())
                .status(fee.getStatus() == null ? null : ActivityStatus.valueOf(fee.getStatus().name()))
                .build();
    }

    public edu.hei.school.agricultural.entity.MembershipFee mapMembershipFeeToEntity(CreateMembershipFee dto) {
        return edu.hei.school.agricultural.entity.MembershipFee.builder()
                .eligibleFrom(dto.getEligibleFrom())
                .frequency(dto.getFrequency() == null ? null : edu.hei.school.agricultural.entity.Frequency.valueOf(dto.getFrequency().name()))
                .amount(dto.getAmount())
                .label(dto.getLabel())
                .status(edu.hei.school.agricultural.entity.ActivityStatus.ACTIVE)
                .build();
    }

    public edu.hei.school.agricultural.controller.dto.FinancialAccount mapFinancialAccountToDto(edu.hei.school.agricultural.entity.FinancialAccount account) {
        return edu.hei.school.agricultural.controller.dto.FinancialAccount.builder()
                .id(account.getId())
                .accountType(account.getAccountType() == null ? null : account.getAccountType().name())
                .holderName(account.getHolderName())
                .mobileBankingService(account.getMobileBankingService() == null ? null : account.getMobileBankingService().name())
                .mobileNumber(account.getMobileNumber())
                .bankName(account.getBankName() == null ? null : account.getBankName().name())
                .bankCode(account.getBankCode())
                .bankBranchCode(account.getBankBranchCode())
                .bankAccountNumber(account.getBankAccountNumber())
                .bankAccountKey(account.getBankAccountKey())
                .amount(account.getAmount())
                .build();
    }

    public edu.hei.school.agricultural.controller.dto.CollectivityTransaction mapTransactionToDto(edu.hei.school.agricultural.entity.CollectivityTransaction tx) {
        return edu.hei.school.agricultural.controller.dto.CollectivityTransaction.builder()
                .id(tx.getId())
                .creationDate(tx.getCreationDate())
                .amount(tx.getAmount())
                .paymentMode(tx.getPaymentMode() == null ? null : PaymentMode.valueOf(tx.getPaymentMode().name()))
                .accountCredited(tx.getAccountCredited() == null ? null : mapFinancialAccountToDto(tx.getAccountCredited()))
                .memberDebited(tx.getMemberDebited() == null ? null : memberDtoMapper.mapToDto(tx.getMemberDebited()))
                .build();
    }

    public edu.hei.school.agricultural.controller.dto.CollectivityLocalStatistics mapLocalStatsToDto(edu.hei.school.agricultural.entity.CollectivityLocalStatistics stats) {
        edu.hei.school.agricultural.entity.Member m = stats.getMember();
        MemberDescription desc = MemberDescription.builder()
                .id(m.getId())
                .firstName(m.getFirstName())
                .lastName(m.getLastName())
                .email(m.getEmail())
                .occupation(m.getOccupation() == null ? null : m.getOccupation().name())
                .build();
        return edu.hei.school.agricultural.controller.dto.CollectivityLocalStatistics.builder()
                .memberDescription(desc)
                .earnedAmount(stats.getEarnedAmount())
                .unpaidAmount(stats.getUnpaidAmount())
                .assiduityPercentage(stats.getAssiduityPercentage())
                .build();
    }

    public edu.hei.school.agricultural.controller.dto.CollectivityOverallStatistics mapOverallStatsToDto(edu.hei.school.agricultural.entity.CollectivityOverallStatistics stats) {
        edu.hei.school.agricultural.entity.Collectivity c = stats.getCollectivity();
        CollectivityInformation info = CollectivityInformation.builder()
                .name(c.getName())
                .number(c.getNumber())
                .build();
        return edu.hei.school.agricultural.controller.dto.CollectivityOverallStatistics.builder()
                .collectivityInformation(info)
                .newMembersNumber(stats.getNewMembersNumber())
                .overallMemberCurrentDuePercentage(stats.getOverallMemberCurrentDuePercentage())
                .overallMemberAssiduityPercentage(stats.getOverallMemberAssiduityPercentage())
                .build();
    }

    public edu.hei.school.agricultural.controller.dto.CollectivityActivity mapActivityToDto(edu.hei.school.agricultural.entity.CollectivityActivity activity) {
        return edu.hei.school.agricultural.controller.dto.CollectivityActivity.builder()
                .id(activity.getId())
                .label(activity.getLabel())
                .activityType(activity.getActivityType() == null ? null : activity.getActivityType().name())
                .memberOccupationConcerned(activity.getMemberOccupationConcerned() == null ? null :
                        activity.getMemberOccupationConcerned().stream()
                                .map(o -> MemberOccupation.valueOf(o.name()))
                                .toList())
                .recurrenceRule(activity.getRecurrenceRule() == null ? null :
                        MonthlyRecurrenceRule.builder()
                                .weekOrdinal(activity.getRecurrenceRule().getWeekOrdinal())
                                .dayOfWeek(activity.getRecurrenceRule().getDayOfWeek())
                                .build())
                .executiveDate(activity.getExecutiveDate())
                .build();
    }

    public edu.hei.school.agricultural.entity.CollectivityActivity mapActivityToEntity(CreateCollectivityActivity dto) {
        return edu.hei.school.agricultural.entity.CollectivityActivity.builder()
                .label(dto.getLabel())
                .activityType(dto.getActivityType() == null ? null : edu.hei.school.agricultural.entity.ActivityType.valueOf(dto.getActivityType()))
                .memberOccupationConcerned(dto.getMemberOccupationConcerned() == null ? null :
                        dto.getMemberOccupationConcerned().stream()
                                .map(o -> edu.hei.school.agricultural.entity.MemberOccupation.valueOf(o.name()))
                                .toList())
                .recurrenceRule(dto.getRecurrenceRule() == null ? null :
                        edu.hei.school.agricultural.entity.MonthlyRecurrenceRule.builder()
                                .weekOrdinal(dto.getRecurrenceRule().getWeekOrdinal())
                                .dayOfWeek(dto.getRecurrenceRule().getDayOfWeek())
                                .build())
                .executiveDate(dto.getExecutiveDate())
                .build();
    }

    public ActivityMemberAttendance mapAttendanceToDto(edu.hei.school.agricultural.entity.ActivityAttendance attendance) {
        edu.hei.school.agricultural.entity.Member m = attendance.getMember();
        MemberDescription desc = MemberDescription.builder()
                .id(m.getId())
                .firstName(m.getFirstName())
                .lastName(m.getLastName())
                .email(m.getEmail())
                .occupation(m.getOccupation() == null ? null : m.getOccupation().name())
                .build();
        return ActivityMemberAttendance.builder()
                .id(attendance.getId())
                .memberDescription(desc)
                .attendanceStatus(attendance.getAttendanceStatus() == null ? null : attendance.getAttendanceStatus().name())
                .build();
    }
}
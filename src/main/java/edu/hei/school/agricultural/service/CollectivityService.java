package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.entity.*;
import edu.hei.school.agricultural.exception.BadRequestException;
import edu.hei.school.agricultural.exception.NotFoundException;
import edu.hei.school.agricultural.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class CollectivityService {
    private final CollectivityRepository collectivityRepository;
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final CollectivityTransactionRepository collectivityTransactionRepository;
    private final MemberPaymentRepository memberPaymentRepository;
    private final CollectivityActivityRepository collectivityActivityRepository;
    private final ActivityAttendanceRepository activityAttendanceRepository;

    public List<Collectivity> createCollectivities(List<Collectivity> collectivities) {
        for (Collectivity collectivity : collectivities) {
            if (!collectivity.hasEnoughMembers()) {
                throw new BadRequestException("Collectivity must have at least 10 members, otherwise actual is " + collectivity.getMembers().size());
            }
            collectivity.setId(randomUUID().toString());
        }
        return collectivityRepository.saveAll(collectivities);
    }

    public Collectivity getCollectivityById(String id) {
        return collectivityRepository.findById(id).orElseThrow(() -> new NotFoundException("Collectivity.id= " + id + " not found"));
    }

    public List<MembershipFee> getMembershipFees(String collectivityId) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        return membershipFeeRepository.findAllByCollectivityId(collectivityId);
    }

    public List<MembershipFee> createMembershipFees(String collectivityId, List<MembershipFee> fees) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        for (MembershipFee fee : fees) {
            if (fee.getAmount() == null || fee.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Membership fee amount must be >= 0");
            }
            if (fee.getFrequency() == null) {
                throw new BadRequestException("Membership fee frequency is required");
            }
        }
        return membershipFeeRepository.saveAll(collectivityId, fees);
    }

    public List<CollectivityTransaction> getTransactions(String collectivityId, LocalDate from, LocalDate to) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        return collectivityTransactionRepository.findAllByCollectivityIdBetween(collectivityId, from, to);
    }

    public List<FinancialAccount> getFinancialAccounts(String collectivityId, LocalDate at) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        if (at != null) {
            return financialAccountRepository.findAllByCollectivityIdAt(collectivityId, at);
        }
        return financialAccountRepository.findAllByCollectivityId(collectivityId);
    }

    public List<MemberPayment> createMemberPayments(String memberId, List<MemberPayment> payments) {
        return memberPaymentRepository.saveAll(memberId, payments);
    }

    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId, LocalDate from, LocalDate to) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId)
                .orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        List<Member> members = collectivity.getMembers();
        List<MembershipFee> activeFees = membershipFeeRepository.findActiveByCollectivityId(collectivityId);
        List<CollectivityLocalStatistics> result = new ArrayList<>();
        for (Member member : members) {
            BigDecimal earned = memberPaymentRepository.sumAmountByMemberIdBetween(member.getId(), from, to);
            BigDecimal unpaid = BigDecimal.ZERO;
            for (MembershipFee fee : activeFees) {
                boolean paid = memberPaymentRepository.hasPaidFee(member.getId(), fee.getId(), from, to);
                if (!paid) {
                    unpaid = unpaid.add(fee.getAmount() == null ? BigDecimal.ZERO : fee.getAmount());
                }
            }
            long attended = activityAttendanceRepository.countAttendedByMemberIdBetween(member.getId(), from, to);
            long required = activityAttendanceRepository.countRequiredActivitiesByMemberIdBetween(member.getId(), collectivityId, from, to);
            Double assiduity = required == 0 ? null : BigDecimal.valueOf(attended).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(required), 2, RoundingMode.HALF_UP).doubleValue();
            result.add(CollectivityLocalStatistics.builder()
                    .member(member)
                    .earnedAmount(earned)
                    .unpaidAmount(unpaid)
                    .assiduityPercentage(assiduity)
                    .build());
        }
        return result;
    }

    public List<CollectivityOverallStatistics> getOverallStatistics(LocalDate from, LocalDate to) {
        List<Collectivity> allCollectivities = collectivityRepository.findAll();
        List<CollectivityOverallStatistics> result = new ArrayList<>();
        for (Collectivity collectivity : allCollectivities) {
            List<Member> members = collectivity.getMembers();
            int newMembers = collectivityRepository.countNewMembersBetween(collectivity.getId(), from, to);
            List<MembershipFee> activeFees = membershipFeeRepository.findActiveByCollectivityId(collectivity.getId());
            int upToDateCount = 0;
            for (Member member : members) {
                boolean upToDate = true;
                for (MembershipFee fee : activeFees) {
                    if (!memberPaymentRepository.hasPaidFee(member.getId(), fee.getId(), from, to)) {
                        upToDate = false;
                        break;
                    }
                }
                if (upToDate) upToDateCount++;
            }
            double duePercentage = members.isEmpty() ? 0.0 :
                    BigDecimal.valueOf(upToDateCount).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP).doubleValue();
            long totalAttended = activityAttendanceRepository.countAttendedForCollectivityBetween(collectivity.getId(), from, to);
            long totalRequired = activityAttendanceRepository.countTotalRequiredAttendanceForCollectivityBetween(collectivity.getId(), from, to);
            Double assiduity = totalRequired == 0 ? null :
                    BigDecimal.valueOf(totalAttended).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalRequired), 2, RoundingMode.HALF_UP).doubleValue();
            result.add(CollectivityOverallStatistics.builder()
                    .collectivity(collectivity)
                    .newMembersNumber(newMembers)
                    .overallMemberCurrentDuePercentage(duePercentage)
                    .overallMemberAssiduityPercentage(assiduity)
                    .build());
        }
        return result;
    }

    public List<CollectivityActivity> getActivities(String collectivityId) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        return collectivityActivityRepository.findAllByCollectivityId(collectivityId);
    }

    public List<CollectivityActivity> createActivities(String collectivityId, List<CollectivityActivity> activities) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        for (CollectivityActivity activity : activities) {
            if (activity.getRecurrenceRule() != null && activity.getExecutiveDate() != null) {
                throw new BadRequestException("Cannot provide both recurrenceRule and executiveDate for an activity");
            }
        }
        return collectivityActivityRepository.saveAll(collectivityId, activities);
    }

    public List<ActivityAttendance> createAttendance(String collectivityId, String activityId, List<ActivityAttendance> attendances) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        collectivityActivityRepository.findById(activityId).orElseThrow(() -> new NotFoundException("Activity.id= " + activityId + " not found"));
        return activityAttendanceRepository.saveAll(activityId, attendances);
    }

    public List<ActivityAttendance> getAttendance(String collectivityId, String activityId) {
        collectivityRepository.findById(collectivityId).orElseThrow(() -> new NotFoundException("Collectivity.id= " + collectivityId + " not found"));
        collectivityActivityRepository.findById(activityId).orElseThrow(() -> new NotFoundException("Activity.id= " + activityId + " not found"));
        return activityAttendanceRepository.findAllByActivityId(activityId);
    }
}
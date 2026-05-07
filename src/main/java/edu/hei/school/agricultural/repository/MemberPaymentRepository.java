package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberPaymentRepository {
    private final Connection connection;
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;

    public List<MemberPayment> saveAll(String memberId, List<MemberPayment> payments) {
        List<MemberPayment> saved = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                insert into "member_payment" (id, member_id, membership_fee_id, account_credited_id, amount, payment_mode, creation_date)
                values (?, ?, ?, ?, ?, ?::payment_mode, ?)
                """)) {
            for (MemberPayment payment : payments) {
                payment.setId(UUID.randomUUID().toString());
                payment.setMemberId(memberId);
                payment.setCreationDate(LocalDate.now());
                ps.setString(1, payment.getId());
                ps.setString(2, memberId);
                ps.setString(3, payment.getMembershipFee() == null ? null : payment.getMembershipFee().getId());
                ps.setString(4, payment.getAccountCredited() == null ? null : payment.getAccountCredited().getId());
                ps.setBigDecimal(5, payment.getAmount());
                ps.setString(6, payment.getPaymentMode().name());
                ps.setDate(7, Date.valueOf(payment.getCreationDate()));
                ps.addBatch();
                financialAccountRepository.addAmount(payment.getAccountCredited().getId(), payment.getAmount());
            }
            ps.executeBatch();
            for (MemberPayment payment : payments) {
                findById(payment.getId()).ifPresent(saved::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    public Optional<MemberPayment> findById(String id) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, member_id, membership_fee_id, account_credited_id, amount, payment_mode, creation_date
                from "member_payment"
                where id = ?
                """)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<MemberPayment> findAllByMemberId(String memberId) {
        List<MemberPayment> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, member_id, membership_fee_id, account_credited_id, amount, payment_mode, creation_date
                from "member_payment"
                where member_id = ?
                """)) {
            ps.setString(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public java.math.BigDecimal sumAmountByMemberIdBetween(String memberId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select COALESCE(sum(amount), 0) as total
                from "member_payment"
                where member_id = ? and creation_date >= ? and creation_date <= ?
                """)) {
            ps.setString(1, memberId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return java.math.BigDecimal.ZERO;
    }

    public boolean hasPaidFee(String memberId, String feeId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select count(*) as cnt from "member_payment"
                where member_id = ? and membership_fee_id = ? and creation_date >= ? and creation_date <= ?
                """)) {
            ps.setString(1, memberId);
            ps.setString(2, feeId);
            ps.setDate(3, Date.valueOf(from));
            ps.setDate(4, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt") > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private MemberPayment mapFromResultSet(ResultSet rs) throws SQLException {
        String feeId = rs.getString("membership_fee_id");
        String accountId = rs.getString("account_credited_id");
        return MemberPayment.builder()
                .id(rs.getString("id"))
                .memberId(rs.getString("member_id"))
                .membershipFee(feeId == null ? null : membershipFeeRepository.findById(feeId).orElse(null))
                .accountCredited(accountId == null ? null : financialAccountRepository.findById(accountId).orElse(null))
                .amount(rs.getBigDecimal("amount"))
                .paymentMode(rs.getString("payment_mode") == null ? null : PaymentMode.valueOf(rs.getString("payment_mode")))
                .creationDate(rs.getDate("creation_date") == null ? null : rs.getDate("creation_date").toLocalDate())
                .build();
    }
}
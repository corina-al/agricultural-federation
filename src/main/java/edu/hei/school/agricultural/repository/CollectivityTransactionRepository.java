package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.*;
import edu.hei.school.agricultural.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CollectivityTransactionRepository {
    private final Connection connection;
    private final MemberMapper memberMapper;
    private final FinancialAccountRepository financialAccountRepository;

    public void save(CollectivityTransaction transaction) {
        transaction.setId(UUID.randomUUID().toString());
        try (PreparedStatement ps = connection.prepareStatement("""
                insert into "collectivity_transaction" (id, collectivity_id, member_id, account_credited_id, amount, payment_mode, creation_date)
                values (?, ?, ?, ?, ?, ?::payment_mode, ?)
                """)) {
            ps.setString(1, transaction.getId());
            ps.setString(2, transaction.getCollectivityId());
            ps.setString(3, transaction.getMemberDebited() == null ? null : transaction.getMemberDebited().getId());
            ps.setString(4, transaction.getAccountCredited() == null ? null : transaction.getAccountCredited().getId());
            ps.setBigDecimal(5, transaction.getAmount());
            ps.setString(6, transaction.getPaymentMode().name());
            ps.setDate(7, Date.valueOf(transaction.getCreationDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CollectivityTransaction> findAllByCollectivityIdBetween(String collectivityId, LocalDate from, LocalDate to) {
        List<CollectivityTransaction> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select ct.id, ct.collectivity_id, ct.member_id, ct.account_credited_id, ct.amount, ct.payment_mode, ct.creation_date,
                       m.id as m_id, m.first_name, m.last_name, m.birth_date, m.gender, m.phone_number, m.email, m.address, m.profession, m.occupation, m.registration_fee_paid, m.membership_dues_paid
                from "collectivity_transaction" ct
                left join "member" m on ct.member_id = m.id
                where ct.collectivity_id = ? and ct.creation_date >= ? and ct.creation_date <= ?
                """)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private CollectivityTransaction mapFromResultSet(ResultSet rs) throws SQLException {
        String accountId = rs.getString("account_credited_id");
        Member member = null;
        if (rs.getString("member_id") != null) {
            member = memberMapper.mapFromResultSet(rs);
        }
        return CollectivityTransaction.builder()
                .id(rs.getString("id"))
                .collectivityId(rs.getString("collectivity_id"))
                .memberDebited(member)
                .accountCredited(accountId == null ? null : financialAccountRepository.findById(accountId).orElse(null))
                .amount(rs.getBigDecimal("amount"))
                .paymentMode(rs.getString("payment_mode") == null ? null : PaymentMode.valueOf(rs.getString("payment_mode")))
                .creationDate(rs.getDate("creation_date") == null ? null : rs.getDate("creation_date").toLocalDate())
                .build();
    }
}
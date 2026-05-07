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
public class FinancialAccountRepository {
    private final Connection connection;

    public FinancialAccount save(FinancialAccount account) {
        account.setId(UUID.randomUUID().toString());
        try (PreparedStatement ps = connection.prepareStatement("""
                insert into "financial_account" (id, collectivity_id, account_type, holder_name,
                    mobile_banking_service, mobile_number, bank_name, bank_code, bank_branch_code,
                    bank_account_number, bank_account_key, amount)
                values (?, ?, ?::financial_account_type, ?, ?::mobile_banking_service, ?, ?::bank, ?, ?, ?, ?, ?)
                """)) {
            ps.setString(1, account.getId());
            ps.setString(2, account.getCollectivityId());
            ps.setString(3, account.getAccountType().name());
            ps.setString(4, account.getHolderName());
            ps.setString(5, account.getMobileBankingService() == null ? null : account.getMobileBankingService().name());
            ps.setString(6, account.getMobileNumber());
            ps.setString(7, account.getBankName() == null ? null : account.getBankName().name());
            ps.setObject(8, account.getBankCode());
            ps.setObject(9, account.getBankBranchCode());
            ps.setObject(10, account.getBankAccountNumber());
            ps.setObject(11, account.getBankAccountKey());
            ps.setBigDecimal(12, account.getAmount());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findById(account.getId()).orElseThrow();
    }

    public void addAmount(String accountId, java.math.BigDecimal amount) {
        try (PreparedStatement ps = connection.prepareStatement("""
                update "financial_account" set amount = amount + ? where id = ?
                """)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FinancialAccount> findAllByCollectivityId(String collectivityId) {
        return findAllByCollectivityIdAt(collectivityId, null);
    }

    public List<FinancialAccount> findAllByCollectivityIdAt(String collectivityId, LocalDate at) {
        List<FinancialAccount> list = new ArrayList<>();
        String sql;
        if (at != null) {
            sql = """
                    select fa.id, fa.collectivity_id, fa.account_type, fa.holder_name,
                           fa.mobile_banking_service, fa.mobile_number, fa.bank_name, fa.bank_code,
                           fa.bank_branch_code, fa.bank_account_number, fa.bank_account_key,
                           COALESCE((select sum(ct.amount) from collectivity_transaction ct where ct.account_credited_id = fa.id and ct.creation_date <= ?), 0) as amount
                    from "financial_account" fa
                    where fa.collectivity_id = ?
                    """;
        } else {
            sql = """
                    select id, collectivity_id, account_type, holder_name,
                           mobile_banking_service, mobile_number, bank_name, bank_code,
                           bank_branch_code, bank_account_number, bank_account_key, amount
                    from "financial_account"
                    where collectivity_id = ?
                    """;
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (at != null) {
                ps.setDate(1, Date.valueOf(at));
                ps.setString(2, collectivityId);
            } else {
                ps.setString(1, collectivityId);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Optional<FinancialAccount> findById(String id) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, collectivity_id, account_type, holder_name,
                       mobile_banking_service, mobile_number, bank_name, bank_code,
                       bank_branch_code, bank_account_number, bank_account_key, amount
                from "financial_account"
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

    private FinancialAccount mapFromResultSet(ResultSet rs) throws SQLException {
        return FinancialAccount.builder()
                .id(rs.getString("id"))
                .collectivityId(rs.getString("collectivity_id"))
                .accountType(rs.getString("account_type") == null ? null : FinancialAccountType.valueOf(rs.getString("account_type")))
                .holderName(rs.getString("holder_name"))
                .mobileBankingService(rs.getString("mobile_banking_service") == null ? null : MobileBankingService.valueOf(rs.getString("mobile_banking_service")))
                .mobileNumber(rs.getString("mobile_number"))
                .bankName(rs.getString("bank_name") == null ? null : Bank.valueOf(rs.getString("bank_name")))
                .bankCode(rs.getObject("bank_code") == null ? null : rs.getInt("bank_code"))
                .bankBranchCode(rs.getObject("bank_branch_code") == null ? null : rs.getInt("bank_branch_code"))
                .bankAccountNumber(rs.getObject("bank_account_number") == null ? null : rs.getLong("bank_account_number"))
                .bankAccountKey(rs.getObject("bank_account_key") == null ? null : rs.getInt("bank_account_key"))
                .amount(rs.getBigDecimal("amount"))
                .build();
    }
}
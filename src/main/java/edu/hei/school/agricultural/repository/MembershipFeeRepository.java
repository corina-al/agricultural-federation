package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.ActivityStatus;
import edu.hei.school.agricultural.entity.Frequency;
import edu.hei.school.agricultural.entity.MembershipFee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembershipFeeRepository {
    private final Connection connection;

    public List<MembershipFee> saveAll(String collectivityId, List<MembershipFee> fees) {
        List<MembershipFee> saved = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                insert into "membership_fee" (id, collectivity_id, label, eligible_from, frequency, amount, status)
                values (?, ?, ?, ?, ?::frequency, ?, ?::activity_status)
                """)) {
            for (MembershipFee fee : fees) {
                fee.setId(UUID.randomUUID().toString());
                fee.setCollectivityId(collectivityId);
                ps.setString(1, fee.getId());
                ps.setString(2, fee.getCollectivityId());
                ps.setString(3, fee.getLabel());
                ps.setDate(4, fee.getEligibleFrom() == null ? null : java.sql.Date.valueOf(fee.getEligibleFrom()));
                ps.setString(5, fee.getFrequency().name());
                ps.setBigDecimal(6, fee.getAmount());
                ps.setString(7, fee.getStatus() == null ? ActivityStatus.ACTIVE.name() : fee.getStatus().name());
                ps.addBatch();
            }
            ps.executeBatch();
            for (MembershipFee fee : fees) {
                findById(fee.getId()).ifPresent(saved::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    public List<MembershipFee> findAllByCollectivityId(String collectivityId) {
        List<MembershipFee> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, collectivity_id, label, eligible_from, frequency, amount, status
                from "membership_fee"
                where collectivity_id = ?
                """)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<MembershipFee> findActiveByCollectivityId(String collectivityId) {
        List<MembershipFee> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, collectivity_id, label, eligible_from, frequency, amount, status
                from "membership_fee"
                where collectivity_id = ? and status = 'ACTIVE'
                """)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Optional<MembershipFee> findById(String id) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, collectivity_id, label, eligible_from, frequency, amount, status
                from "membership_fee"
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

    private MembershipFee mapFromResultSet(ResultSet rs) throws SQLException {
        return MembershipFee.builder()
                .id(rs.getString("id"))
                .collectivityId(rs.getString("collectivity_id"))
                .label(rs.getString("label"))
                .eligibleFrom(rs.getDate("eligible_from") == null ? null : rs.getDate("eligible_from").toLocalDate())
                .frequency(rs.getString("frequency") == null ? null : Frequency.valueOf(rs.getString("frequency")))
                .amount(rs.getBigDecimal("amount"))
                .status(rs.getString("status") == null ? null : ActivityStatus.valueOf(rs.getString("status")))
                .build();
    }
}
package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.CollectivityStructure;
import edu.hei.school.agricultural.mapper.CollectivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectivityRepository {
    private final Connection connection;
    private final CollectivityMapper collectivityMapper;

    public List<Collectivity> saveAll(List<Collectivity> collectivities) {
        List<Collectivity> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        insert into "collectivity" (id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id)
                        values (?, ?, ?, ?, ?, ?, ?, ?)
                        on conflict (id) do update set name = excluded.name,
                                                       number = excluded.number,
                                                       location = excluded.location,
                                                       president_id = excluded.president_id,
                                                       vice_president_id = excluded.vice_president_id,
                                                       treasurer_id = excluded.treasurer_id,
                                                       secretary_id = excluded.secretary_id
                        """)) {
            for (Collectivity collectivity : collectivities) {
                CollectivityStructure collectivityStructure = collectivity.getCollectivityStructure();
                preparedStatement.setString(1, collectivity.getId());
                preparedStatement.setString(2, collectivity.getName());
                preparedStatement.setObject(3, collectivity.getNumber());
                preparedStatement.setObject(4, collectivity.getLocation());
                preparedStatement.setString(5, collectivityStructure.getPresident() == null ? null : collectivityStructure.getPresident().getId());
                preparedStatement.setString(6, collectivityStructure.getVicePresident() == null ? null : collectivityStructure.getVicePresident().getId());
                preparedStatement.setString(7, collectivityStructure.getTreasurer() == null ? null : collectivityStructure.getTreasurer().getId());
                preparedStatement.setString(8, collectivityStructure.getSecretary() == null ? null : collectivityStructure.getSecretary().getId());
                preparedStatement.addBatch();
            }
            var executedRow = preparedStatement.executeBatch();
            for (int i = 0; i < executedRow.length; i++) {
                memberList.add(findById(collectivities.get(i).getId()).orElseThrow());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return memberList;
    }

    public Optional<Collectivity> findById(String id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id
                from "collectivity"
                where id = ?
                """)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(collectivityMapper.mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<Collectivity> findAll() {
        List<Collectivity> collectivities = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id
                from "collectivity"
                """)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                collectivities.add(collectivityMapper.mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return collectivities;
    }

    public List<Collectivity> findAllByMemberId(String memberId) {
        List<Collectivity> collectivities = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id
                from "collectivity"
                join "collectivity_member" on collectivity.id = collectivity_member.collectivity_id
                where collectivity_member.member_id = ?
                """)) {
            preparedStatement.setString(1, memberId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                collectivities.add(collectivityMapper.mapFromResultSet(resultSet));
            }
            return collectivities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countNewMembersBetween(String collectivityId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select count(distinct cm.member_id) as cnt
                from "collectivity_member" cm
                join "member" m on cm.member_id = m.id
                where cm.collectivity_id = ?
                  and m.id in (
                      select mp.member_id from "member_payment" mp
                      where mp.creation_date >= ? and mp.creation_date <= ?
                      group by mp.member_id
                      having count(*) > 0
                  )
                """)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
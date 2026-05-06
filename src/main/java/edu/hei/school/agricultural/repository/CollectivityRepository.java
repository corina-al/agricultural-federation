package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.CollectivityStructure;
import edu.hei.school.agricultural.mapper.CollectivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                        insert into collectivity (id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id) 
                        values (?, ?, ?, ? ?, ?, ?, ?, ?) 
                        on conflict (id) do update set name = excluded.first_name  
                                                                        and number = excluded.number 
                                                                        and location = excluded.location 
                                                                        and president_id = excluded.president_id 
                                                                        and treasurer_id = excluded.treasurer_id 
                                                                        and secretary_id = excluded.secretary_id 
                        """)) {
            for (Collectivity collectivity : collectivities) {
                CollectivityStructure collectivityStructure = collectivity.getCollectivityStructure();
                preparedStatement.setString(1, collectivity.getId());
                preparedStatement.setString(2, collectivity.getName());
                preparedStatement.setInt(3, collectivity.getNumber());
                preparedStatement.setObject(4, collectivity.getLocation());
                preparedStatement.setString(5, collectivityStructure.getPresident().getId());
                preparedStatement.setString(6, collectivityStructure.getVicePresident().getId());
                preparedStatement.setString(7, collectivityStructure.getTreasurer().getId());
                preparedStatement.setString(8, collectivityStructure.getSecretary().getId());
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
                from collectivity
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
}

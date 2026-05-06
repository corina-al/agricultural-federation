package edu.hei.school.agricultural.mapper;

import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.CollectivityStructure;
import edu.hei.school.agricultural.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class CollectivityMapper {
    private final MemberRepository memberRepository;

    public Collectivity mapFromResultSet(ResultSet resultSet) throws SQLException {
        var collectivity = Collectivity.builder()
                .id(resultSet.getString("id"))
                .name(resultSet.getString("name"))
                .number(resultSet.getInt("number"))
                .location(resultSet.getString("location"))
                .collectivityStructure(CollectivityStructure.builder()
                        .president(resultSet.getString("president_id") == null ? null : memberRepository.findById(resultSet.getString("president_id")).orElse(null))
                        .vicePresident(resultSet.getString("vice_president_id") == null ? null : memberRepository.findById(resultSet.getString("vice_president_id")).orElse(null))
                        .treasurer(resultSet.getString("treasurer_id") == null ? null : memberRepository.findById(resultSet.getString("treasurer_id")).orElse(null))
                        .secretary(resultSet.getString("secretary_id") == null ? null : memberRepository.findById(resultSet.getString("secretary_id")).orElse(null))
                        .build())
                .build();
        collectivity.addMembers(memberRepository.findAllByIdCollectivity(collectivity.getId()));
        return collectivity;
    }
}

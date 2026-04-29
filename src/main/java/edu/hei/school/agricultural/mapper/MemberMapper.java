package edu.hei.school.agricultural.mapper;

import edu.hei.school.agricultural.entity.Member;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MemberMapper {
    public Member mapFromResultSet(ResultSet resultSet) throws SQLException {
        return Member.builder()
                .id(resultSet.getString("id"))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .build();
    }
}

package edu.hei.school.agricultural.mapper;

import edu.hei.school.agricultural.entity.Gender;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.entity.MemberOccupation;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MemberMapper {
    public Member mapFromResultSet(ResultSet resultSet) {
        try {
            return Member.builder()
                    .id(resultSet.getString("id"))
                    .firstName(resultSet.getString("first_name"))
                    .lastName(resultSet.getString("last_name"))
                    .birthDate(resultSet.getDate("birth_date") == null ? null : resultSet.getDate("birth_date").toLocalDate())
                    .gender(resultSet.getString("gender") == null ? null : Gender.valueOf(resultSet.getString("gender")))
                    .phoneNumber(resultSet.getString("phone_number"))
                    .email(resultSet.getString("email"))
                    .occupation(resultSet.getString("occupation") == null ? null : MemberOccupation.valueOf(resultSet.getString("occupation")))
                    .registrationFeePaid(resultSet.getBoolean("registration_fee_paid"))
                    .membershipDuesPaid(resultSet.getBoolean("membership_dues_paid"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

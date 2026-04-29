package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.mapper.MemberMapper;
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
public class MemberRepository {
    private final Connection connection;
    private final MemberMapper memberMapper;

    public List<Member> saveAll(List<Member> members) {
        List<Member> memberList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        insert into member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation, registration_fee_paid, membership_dues_paid) 
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
                        on conflict (id) do update set first_name = excluded.first_name 
                        returning id;
                        """)) {
            for (Member member : members) {
                preparedStatement.setString(1, member.getId());
                preparedStatement.setString(2, member.getFirstName());
                preparedStatement.setString(3, member.getLastName());
                preparedStatement.setDate(4, java.sql.Date.valueOf(member.getBirthDate()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                memberList.add(findById(resultSet.getString("id")).orElseThrow());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return memberList;
    }

    public Optional<Member> findById(String id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                """)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(memberMapper.mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}

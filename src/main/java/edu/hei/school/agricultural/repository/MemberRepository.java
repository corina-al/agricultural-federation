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
                        values (?, ?, ?, ?, ?::gender, ?, ?, ?, ?, ?, ?, ?) 
                        on conflict (id) do update set first_name = excluded.first_name  
                                                                        and last_name = excluded.last_name 
                                                                        and birth_date = excluded.birth_date 
                                                                        and gender = excluded.gender 
                                                                        and phone_number = excluded.phone_number 
                                                                        and email = excluded.email 
                                                                        and occupation=excluded.occupation
                        returning id;
                        """)) {
            for (Member member : members) {
                preparedStatement.setString(1, member.getId());
                preparedStatement.setString(2, member.getFirstName());
                preparedStatement.setString(3, member.getLastName());
                preparedStatement.setDate(4, java.sql.Date.valueOf(member.getBirthDate()));
                preparedStatement.setObject(5, member.getGender());
                preparedStatement.setString(6, member.getAddress());
                preparedStatement.setString(7, member.getProfession());
                preparedStatement.setString(8, member.getPhoneNumber());
                preparedStatement.setString(9, member.getEmail());
                preparedStatement.setObject(10, member.getOccupation());
                preparedStatement.setObject(11, member.getRegistrationFeePaid());
                preparedStatement.setObject(12, member.getMembershipDuesPaid());
                preparedStatement.addBatch();
            }
            var executedRow = preparedStatement.executeBatch();
            for (int i = 0; i < executedRow.length; i++) {
                memberList.add(findById(members.get(i).getId()).orElseThrow());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return memberList;
    }

    public Optional<Member> findById(String id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, first_name, last_name, birth_date, gender, phone_number, email, occupation,registration_fee_paid, membership_dues_paid
                from member
                where id = ?
                """)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(memberMapper.mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<Member> findAllByIdCollectivity(String idCollectivity) {
        List<Member> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, first_name, last_name, birth_date, gender, phone_number, email, occupation,registration_fee_paid, membership_dues_paid
                from member 
                    join collectivity_member on member.id = collectivity_member.member_id
                    join collectivity on collectivity.id = collectivity_member.collectivity_id
                where collectivity_member.collectivity_id = ?
                """)) {
            preparedStatement.setString(1, idCollectivity);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var memberMapped = memberMapper.mapFromResultSet(resultSet);
                memberMapped.setReferees(findRefereesByIdMember(memberMapped.getId()));

                memberList.add(memberMapped);
            }
            return memberList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Member> findRefereesByIdMember(String idMember) {
        List<Member> memberList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, first_name, last_name, birth_date, gender, phone_number, email, occupation,registration_fee_paid, membership_dues_paid
                from member 
                    join member_referee on member.id = member_referee.member_referee_id
                
                where member_referee.member_refered_id = ?
                """)) {
            preparedStatement.setString(1, idMember);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                memberList.add(memberMapper.mapFromResultSet(resultSet));
            }
            return memberList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}

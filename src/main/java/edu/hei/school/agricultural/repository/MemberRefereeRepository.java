package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.util.UUID.randomUUID;

@Repository
@RequiredArgsConstructor
public class MemberRefereeRepository {
    private final Connection connection;

    public void attachMemberReferee(Member memberReferee, Member memberRefereed) {
        String sql = """
                    insert into member_referee (id, member_referee_id, member_refereed_id)
                    values (?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, randomUUID().toString());
            ps.setString(2, memberReferee.getId());
            ps.setString(3, memberRefereed.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

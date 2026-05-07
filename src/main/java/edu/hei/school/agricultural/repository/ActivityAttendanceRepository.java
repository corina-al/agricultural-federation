package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.ActivityAttendance;
import edu.hei.school.agricultural.entity.AttendanceStatus;
import edu.hei.school.agricultural.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ActivityAttendanceRepository {
    private final Connection connection;
    private final MemberMapper memberMapper;

    public List<ActivityAttendance> saveAll(String activityId, List<ActivityAttendance> attendances) {
        List<ActivityAttendance> saved = new ArrayList<>();
        try (PreparedStatement checkPs = connection.prepareStatement("""
                select id, attendance_status from "activity_attendance" where activity_id = ? and member_id = ?
                """);
             PreparedStatement insertPs = connection.prepareStatement("""
                insert into "activity_attendance" (id, activity_id, member_id, attendance_status)
                values (?, ?, ?, ?::attendance_status)
                """);
             PreparedStatement updatePs = connection.prepareStatement("""
                update "activity_attendance" set attendance_status = ?::attendance_status where id = ?
                """)) {
            for (ActivityAttendance attendance : attendances) {
                checkPs.setString(1, activityId);
                checkPs.setString(2, attendance.getMember().getId());
                ResultSet existing = checkPs.executeQuery();
                if (existing.next()) {
                    String currentStatus = existing.getString("attendance_status");
                    if (!"UNDEFINED".equals(currentStatus)) {
                        throw new edu.hei.school.agricultural.exception.BadRequestException(
                                "Attendance for member " + attendance.getMember().getId() + " is already confirmed and cannot be changed");
                    }
                    updatePs.setString(1, attendance.getAttendanceStatus().name());
                    updatePs.setString(2, existing.getString("id"));
                    updatePs.executeUpdate();
                    attendance.setId(existing.getString("id"));
                } else {
                    attendance.setId(UUID.randomUUID().toString());
                    insertPs.setString(1, attendance.getId());
                    insertPs.setString(2, activityId);
                    insertPs.setString(3, attendance.getMember().getId());
                    insertPs.setString(4, attendance.getAttendanceStatus().name());
                    insertPs.addBatch();
                }
            }
            insertPs.executeBatch();
            saved.addAll(findAllByActivityId(activityId));
        } catch (edu.hei.school.agricultural.exception.BadRequestException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    public List<ActivityAttendance> findAllByActivityId(String activityId) {
        List<ActivityAttendance> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select aa.id, aa.activity_id, aa.member_id, aa.attendance_status,
                       m.id as m_id, m.first_name, m.last_name, m.birth_date, m.gender, m.phone_number, m.email, m.address, m.profession, m.occupation, m.registration_fee_paid, m.membership_dues_paid
                from "activity_attendance" aa
                join "member" m on aa.member_id = m.id
                where aa.activity_id = ?
                """)) {
            ps.setString(1, activityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public long countAttendedByMemberIdBetween(String memberId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select count(*) as cnt
                from "activity_attendance" aa
                join "collectivity_activity" ca on aa.activity_id = ca.id
                where aa.member_id = ? and aa.attendance_status = 'ATTENDED'
                  and (ca.executive_date >= ? and ca.executive_date <= ?)
                """)) {
            ps.setString(1, memberId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("cnt");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public long countRequiredActivitiesByMemberIdBetween(String memberId, String collectivityId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select count(*) as cnt
                from "collectivity_activity" ca
                join "member" m on m.id = ?
                where ca.collectivity_id = ?
                  and (ca.executive_date >= ? and ca.executive_date <= ?)
                  and (ca.member_occupations_concerned is null
                       or ca.member_occupations_concerned = ''
                       or ca.member_occupations_concerned like concat('%', m.occupation::text, '%'))
                """)) {
            ps.setString(1, memberId);
            ps.setString(2, collectivityId);
            ps.setDate(3, Date.valueOf(from));
            ps.setDate(4, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("cnt");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public long countAttendedForCollectivityBetween(String collectivityId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select count(*) as cnt
                from "activity_attendance" aa
                join "collectivity_activity" ca on aa.activity_id = ca.id
                join "collectivity_member" cm on cm.member_id = aa.member_id and cm.collectivity_id = ca.collectivity_id
                where ca.collectivity_id = ? and aa.attendance_status = 'ATTENDED'
                  and (ca.executive_date >= ? and ca.executive_date <= ?)
                """)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("cnt");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public long countTotalRequiredAttendanceForCollectivityBetween(String collectivityId, LocalDate from, LocalDate to) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select count(*) as cnt
                from "activity_attendance" aa
                join "collectivity_activity" ca on aa.activity_id = ca.id
                join "collectivity_member" cm on cm.member_id = aa.member_id and cm.collectivity_id = ca.collectivity_id
                where ca.collectivity_id = ?
                  and (ca.executive_date >= ? and ca.executive_date <= ?)
                  and aa.attendance_status in ('ATTENDED', 'MISSING')
                """)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("cnt");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private ActivityAttendance mapFromResultSet(ResultSet rs) throws SQLException {
        var member = memberMapper.mapFromResultSet(rs);
        return ActivityAttendance.builder()
                .id(rs.getString("id"))
                .activityId(rs.getString("activity_id"))
                .member(member)
                .attendanceStatus(rs.getString("attendance_status") == null ? null : AttendanceStatus.valueOf(rs.getString("attendance_status")))
                .build();
    }
}
package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CollectivityActivityRepository {
    private final Connection connection;

    public List<CollectivityActivity> saveAll(String collectivityId, List<CollectivityActivity> activities) {
        List<CollectivityActivity> saved = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                insert into "collectivity_activity" (id, collectivity_id, label, activity_type, member_occupations_concerned, recurrence_week_ordinal, recurrence_day_of_week, executive_date)
                values (?, ?, ?, ?::activity_type, ?, ?, ?, ?)
                """)) {
            for (CollectivityActivity activity : activities) {
                activity.setId(UUID.randomUUID().toString());
                activity.setCollectivityId(collectivityId);
                String occupations = activity.getMemberOccupationConcerned() == null ? null :
                        activity.getMemberOccupationConcerned().stream()
                                .map(MemberOccupation::name)
                                .collect(Collectors.joining(","));
                ps.setString(1, activity.getId());
                ps.setString(2, collectivityId);
                ps.setString(3, activity.getLabel());
                ps.setString(4, activity.getActivityType() == null ? null : activity.getActivityType().name());
                ps.setString(5, occupations);
                if (activity.getRecurrenceRule() != null) {
                    ps.setObject(6, activity.getRecurrenceRule().getWeekOrdinal());
                    ps.setString(7, activity.getRecurrenceRule().getDayOfWeek());
                } else {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.VARCHAR);
                }
                ps.setDate(8, activity.getExecutiveDate() == null ? null : Date.valueOf(activity.getExecutiveDate()));
                ps.addBatch();
            }
            ps.executeBatch();
            for (CollectivityActivity activity : activities) {
                findById(activity.getId()).ifPresent(saved::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    public List<CollectivityActivity> findAllByCollectivityId(String collectivityId) {
        List<CollectivityActivity> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, collectivity_id, label, activity_type, member_occupations_concerned, recurrence_week_ordinal, recurrence_day_of_week, executive_date
                from "collectivity_activity"
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

    public Optional<CollectivityActivity> findById(String id) {
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, collectivity_id, label, activity_type, member_occupations_concerned, recurrence_week_ordinal, recurrence_day_of_week, executive_date
                from "collectivity_activity"
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

    private CollectivityActivity mapFromResultSet(ResultSet rs) throws SQLException {
        String occupationsStr = rs.getString("member_occupations_concerned");
        List<MemberOccupation> occupations = null;
        if (occupationsStr != null && !occupationsStr.isBlank()) {
            occupations = Arrays.stream(occupationsStr.split(","))
                    .map(MemberOccupation::valueOf)
                    .collect(Collectors.toList());
        }
        Integer weekOrdinal = rs.getObject("recurrence_week_ordinal") == null ? null : rs.getInt("recurrence_week_ordinal");
        String dayOfWeek = rs.getString("recurrence_day_of_week");
        MonthlyRecurrenceRule rule = (weekOrdinal != null || dayOfWeek != null)
                ? MonthlyRecurrenceRule.builder().weekOrdinal(weekOrdinal).dayOfWeek(dayOfWeek).build()
                : null;
        return CollectivityActivity.builder()
                .id(rs.getString("id"))
                .collectivityId(rs.getString("collectivity_id"))
                .label(rs.getString("label"))
                .activityType(rs.getString("activity_type") == null ? null : ActivityType.valueOf(rs.getString("activity_type")))
                .memberOccupationConcerned(occupations)
                .recurrenceRule(rule)
                .executiveDate(rs.getDate("executive_date") == null ? null : rs.getDate("executive_date").toLocalDate())
                .build();
    }
}
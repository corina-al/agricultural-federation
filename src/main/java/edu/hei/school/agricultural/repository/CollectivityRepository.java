package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.Collectivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public Optional<Collectivity> findById(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.entity.Collectivity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectivityService {

    public List<Collectivity> createCollectivities(List<Collectivity> collectivities) {
        for (Collectivity collectivity : collectivities) {
            if(!collectivity.hasEnoughMembers()) {
                throw new IllegalArgumentException("Collectivity must have at least 10 members");
            }
        }
        throw new UnsupportedOperationException("Not implemented");
    }
}

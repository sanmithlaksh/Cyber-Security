package com.cybershield.portal.repository;

import com.cybershield.portal.model.ThreatIntel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreatIntelRepository extends JpaRepository<ThreatIntel, Long> {
    List<ThreatIntel> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String category, String description);
}

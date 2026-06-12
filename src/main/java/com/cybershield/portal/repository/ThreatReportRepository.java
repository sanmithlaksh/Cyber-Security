package com.cybershield.portal.repository;

import com.cybershield.portal.model.ThreatReport;
import com.cybershield.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreatReportRepository extends JpaRepository<ThreatReport, String> {
    List<ThreatReport> findByUserOrderByDateTimeDesc(User user);
    List<ThreatReport> findAllByOrderByDateTimeDesc();
    List<ThreatReport> findBySeverityOrderByDateTimeDesc(String severity);
    List<ThreatReport> findByStatusOrderByDateTimeDesc(String status);
    
    long countByStatus(String status);
    long countBySeverity(String severity);
    long countByReportIdStartingWith(String prefix);
}

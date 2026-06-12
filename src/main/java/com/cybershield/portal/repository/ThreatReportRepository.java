package com.cybershield.portal.repository;

import com.cybershield.portal.config.FirebaseConfig;
import com.cybershield.portal.model.ThreatReport;
import com.cybershield.portal.model.User;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ThreatReportRepository {

    private final FirebaseConfig firebaseConfig;
    private final Firestore firestore;

    // Local in-memory fallback database
    private static final Map<String, ThreatReport> localDatabase = new ConcurrentHashMap<>();

    public ThreatReportRepository(FirebaseConfig firebaseConfig, java.util.Optional<Firestore> firestore) {
        this.firebaseConfig = firebaseConfig;
        this.firestore = firestore.orElse(null);
    }

    public Optional<ThreatReport> findById(String id) {
        if (id == null) return Optional.empty();

        if (!firebaseConfig.isFirebaseActive()) {
            return Optional.ofNullable(localDatabase.get(id));
        }

        try {
            DocumentSnapshot document = firestore.collection("reports").document(id).get().get();
            if (document.exists()) {
                return Optional.ofNullable(document.toObject(ThreatReport.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findById report error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public ThreatReport save(ThreatReport report) {
        if (report == null || report.getReportId() == null) return null;

        if (!firebaseConfig.isFirebaseActive()) {
            localDatabase.put(report.getReportId(), report);
            return report;
        }

        try {
            firestore.collection("reports").document(report.getReportId()).set(report).get();
            return report;
        } catch (Exception e) {
            System.err.println("Firebase save report error: " + e.getMessage());
            localDatabase.put(report.getReportId(), report);
            return report;
        }
    }

    public List<ThreatReport> findByUserOrderByDateTimeDesc(User user) {
        if (user == null || user.getEmail() == null) return Collections.emptyList();

        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> r.getUser() != null && user.getEmail().equalsIgnoreCase(r.getUser().getEmail()))
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }

        List<ThreatReport> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("reports")
                    .whereEqualTo("user.email", user.getEmail())
                    .orderBy("dateTime", Query.Direction.DESCENDING)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(ThreatReport.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByUser reports error: " + e.getMessage());
            // local fallback logic on error
            return localDatabase.values().stream()
                    .filter(r -> r.getUser() != null && user.getEmail().equalsIgnoreCase(r.getUser().getEmail()))
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public List<ThreatReport> findAllByOrderByDateTimeDesc() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }

        List<ThreatReport> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("reports")
                    .orderBy("dateTime", Query.Direction.DESCENDING)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(ThreatReport.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findAll reports error: " + e.getMessage());
            return localDatabase.values().stream()
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public List<ThreatReport> findBySeverityOrderByDateTimeDesc(String severity) {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> severity.equalsIgnoreCase(r.getSeverity()))
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }

        List<ThreatReport> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("reports")
                    .whereEqualTo("severity", severity)
                    .orderBy("dateTime", Query.Direction.DESCENDING)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(ThreatReport.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findBySeverity reports error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(r -> severity.equalsIgnoreCase(r.getSeverity()))
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public List<ThreatReport> findByStatusOrderByDateTimeDesc(String status) {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }

        List<ThreatReport> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("reports")
                    .whereEqualTo("status", status)
                    .orderBy("dateTime", Query.Direction.DESCENDING)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(ThreatReport.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByStatus reports error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .sorted((r1, r2) -> compareDates(r2.getDateTime(), r1.getDateTime()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public long countByStatus(String status) {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .count();
        }

        try {
            return firestore.collection("reports")
                    .whereEqualTo("status", status)
                    .get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase countByStatus error: " + e.getMessage());
            return 0;
        }
    }

    public long countBySeverity(String severity) {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> severity.equalsIgnoreCase(r.getSeverity()))
                    .count();
        }

        try {
            return firestore.collection("reports")
                    .whereEqualTo("severity", severity)
                    .get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase countBySeverity error: " + e.getMessage());
            return 0;
        }
    }

    public long countByReportIdStartingWith(String prefix) {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.keySet().stream()
                    .filter(id -> id.startsWith(prefix))
                    .count();
        }

        try {
            // Note: startAt/endAt prefix query logic
            return firestore.collection("reports")
                    .orderBy("__name__")
                    .startAt(prefix)
                    .endAt(prefix + "\uf8ff")
                    .get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase countByReportIdStartingWith error: " + e.getMessage());
            return localDatabase.keySet().stream()
                    .filter(id -> id.startsWith(prefix))
                    .count();
        }
    }

    public long count() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.size();
        }

        try {
            return firestore.collection("reports").get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase count reports error: " + e.getMessage());
            return localDatabase.size();
        }
    }

    private int compareDates(String d1, String d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;
        return d1.compareTo(d2);
    }
}

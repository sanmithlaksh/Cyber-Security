package com.cybershield.portal.repository;

import com.cybershield.portal.config.FirebaseConfig;
import com.cybershield.portal.model.QuizResult;
import com.cybershield.portal.model.User;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class QuizResultRepository {

    private final FirebaseConfig firebaseConfig;
    private final Firestore firestore;

    // Local in-memory fallback database
    private static final Map<Long, QuizResult> localDatabase = new ConcurrentHashMap<>();
    private static long idCounter = 1;

    public QuizResultRepository(FirebaseConfig firebaseConfig, java.util.Optional<Firestore> firestore) {
        this.firebaseConfig = firebaseConfig;
        this.firestore = firestore.orElse(null);
    }

    public QuizResult save(QuizResult result) {
        if (result == null) return null;

        if (!firebaseConfig.isFirebaseActive()) {
            if (result.getId() == null) {
                result.setId(idCounter++);
            }
            localDatabase.put(result.getId(), result);
            return result;
        }

        try {
            if (result.getId() == null) {
                result.setId(System.currentTimeMillis() + (long)(Math.random() * 1000));
            }
            firestore.collection("quiz_results").document(String.valueOf(result.getId())).set(result).get();
            return result;
        } catch (Exception e) {
            System.err.println("Firebase save quiz result error: " + e.getMessage());
            localDatabase.put(result.getId(), result);
            return result;
        }
    }

    public List<QuizResult> findByUserOrderByDateCompletedDesc(User user) {
        if (user == null || user.getEmail() == null) return Collections.emptyList();

        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> r.getUser() != null && user.getEmail().equalsIgnoreCase(r.getUser().getEmail()))
                    .sorted((r1, r2) -> compareDates(r2.getDateCompleted(), r1.getDateCompleted()))
                    .collect(Collectors.toList());
        }

        List<QuizResult> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("quiz_results")
                    .whereEqualTo("user.email", user.getEmail())
                    .orderBy("dateCompleted", Query.Direction.DESCENDING)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(QuizResult.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByUser quiz results error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(r -> r.getUser() != null && user.getEmail().equalsIgnoreCase(r.getUser().getEmail()))
                    .sorted((r1, r2) -> compareDates(r2.getDateCompleted(), r1.getDateCompleted()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public Optional<QuizResult> findByCertificateUuid(String certificateUuid) {
        if (certificateUuid == null) return Optional.empty();

        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(r -> certificateUuid.equalsIgnoreCase(r.getCertificateUuid()))
                    .findFirst();
        }

        try {
            QuerySnapshot querySnapshot = firestore.collection("quiz_results")
                    .whereEqualTo("certificateUuid", certificateUuid)
                    .limit(1)
                    .get().get();
            if (!querySnapshot.isEmpty()) {
                return Optional.ofNullable(querySnapshot.getDocuments().get(0).toObject(QuizResult.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByCertificateUuid error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(r -> certificateUuid.equalsIgnoreCase(r.getCertificateUuid()))
                    .findFirst();
        }
        return Optional.empty();
    }

    public long count() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.size();
        }

        try {
            return firestore.collection("quiz_results").get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase count quiz results error: " + e.getMessage());
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

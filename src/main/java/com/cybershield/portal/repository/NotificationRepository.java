package com.cybershield.portal.repository;

import com.cybershield.portal.config.FirebaseConfig;
import com.cybershield.portal.model.Notification;
import com.cybershield.portal.model.User;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class NotificationRepository {

    private final FirebaseConfig firebaseConfig;
    private final Firestore firestore;

    // Local in-memory fallback database
    private static final Map<Long, Notification> localDatabase = new ConcurrentHashMap<>();
    private static long idCounter = 1;

    public NotificationRepository(FirebaseConfig firebaseConfig, java.util.Optional<Firestore> firestore) {
        this.firebaseConfig = firebaseConfig;
        this.firestore = firestore.orElse(null);
    }

    public Notification save(Notification notification) {
        if (notification == null) return null;

        if (!firebaseConfig.isFirebaseActive()) {
            if (notification.getId() == null) {
                notification.setId(idCounter++);
            }
            localDatabase.put(notification.getId(), notification);
            return notification;
        }

        try {
            if (notification.getId() == null) {
                notification.setId(System.currentTimeMillis() + (long)(Math.random() * 1000));
            }
            firestore.collection("notifications").document(String.valueOf(notification.getId())).set(notification).get();
            return notification;
        } catch (Exception e) {
            System.err.println("Firebase save notification error: " + e.getMessage());
            localDatabase.put(notification.getId(), notification);
            return notification;
        }
    }

    public List<Notification> findByUserOrderByDateCreatedDesc(User user) {
        if (user == null || user.getEmail() == null) return Collections.emptyList();

        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(n -> n.getUser() != null && user.getEmail().equalsIgnoreCase(n.getUser().getEmail()))
                    .sorted((n1, n2) -> compareDates(n2.getDateCreated(), n1.getDateCreated()))
                    .collect(Collectors.toList());
        }

        List<Notification> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("notifications")
                    .whereEqualTo("user.email", user.getEmail())
                    .orderBy("dateCreated", Query.Direction.DESCENDING)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(Notification.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByUser notifications error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(n -> n.getUser() != null && user.getEmail().equalsIgnoreCase(n.getUser().getEmail()))
                    .sorted((n1, n2) -> compareDates(n2.getDateCreated(), n1.getDateCreated()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public long countByUserAndReadStatus(User user, boolean readStatus) {
        if (user == null || user.getEmail() == null) return 0;

        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(n -> n.getUser() != null && user.getEmail().equalsIgnoreCase(n.getUser().getEmail()))
                    .filter(n -> readStatus == n.isReadStatus())
                    .count();
        }

        try {
            return firestore.collection("notifications")
                    .whereEqualTo("user.email", user.getEmail())
                    .whereEqualTo("readStatus", readStatus)
                    .get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase countByUserAndReadStatus error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(n -> n.getUser() != null && user.getEmail().equalsIgnoreCase(n.getUser().getEmail()))
                    .filter(n -> readStatus == n.isReadStatus())
                    .count();
        }
    }

    public long count() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.size();
        }

        try {
            return firestore.collection("notifications").get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase count notifications error: " + e.getMessage());
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

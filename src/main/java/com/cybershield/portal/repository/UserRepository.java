package com.cybershield.portal.repository;

import com.cybershield.portal.config.FirebaseConfig;
import com.cybershield.portal.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final FirebaseConfig firebaseConfig;
    private final Firestore firestore;

    // Local in-memory fallback database
    private static final Map<String, User> localDatabase = new ConcurrentHashMap<>();
    private static long idCounter = 1;

    public UserRepository(FirebaseConfig firebaseConfig, java.util.Optional<Firestore> firestore) {
        this.firebaseConfig = firebaseConfig;
        this.firestore = firestore.orElse(null);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String cleanEmail = email.trim().toLowerCase();

        if (!firebaseConfig.isFirebaseActive()) {
            return Optional.ofNullable(localDatabase.get(cleanEmail));
        }

        try {
            DocumentSnapshot document = firestore.collection("users").document(cleanEmail).get().get();
            if (document.exists()) {
                return Optional.ofNullable(document.toObject(User.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByEmail error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public User save(User user) {
        if (user == null || user.getEmail() == null) return null;
        String cleanEmail = user.getEmail().trim().toLowerCase();

        if (!firebaseConfig.isFirebaseActive()) {
            if (user.getId() == null) {
                user.setId(idCounter++);
            }
            localDatabase.put(cleanEmail, user);
            return user;
        }

        try {
            if (user.getId() == null) {
                user.setId(System.currentTimeMillis());
            }
            firestore.collection("users").document(cleanEmail).set(user).get();
            return user;
        } catch (Exception e) {
            System.err.println("Firebase save user error: " + e.getMessage());
            // Failover to local if firestore write crashes
            localDatabase.put(cleanEmail, user);
            return user;
        }
    }

    public long countByRole(String role) {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(u -> role.equals(u.getRole()))
                    .count();
        }

        try {
            QuerySnapshot querySnapshot = firestore.collection("users")
                    .whereEqualTo("role", role)
                    .get().get();
            return querySnapshot.size();
        } catch (Exception e) {
            System.err.println("Firebase countByRole error: " + e.getMessage());
            return 0;
        }
    }

    public List<User> findAll() {
        if (!firebaseConfig.isFirebaseActive()) {
            return new ArrayList<>(localDatabase.values());
        }

        List<User> users = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("users").get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                users.add(doc.toObject(User.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findAll users error: " + e.getMessage());
        }
        return users;
    }

    public long count() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.size();
        }

        try {
            return firestore.collection("users").get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase count users error: " + e.getMessage());
            return localDatabase.size();
        }
    }
}

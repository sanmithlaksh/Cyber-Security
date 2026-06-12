package com.cybershield.portal.repository;

import com.cybershield.portal.config.FirebaseConfig;
import com.cybershield.portal.model.ThreatIntel;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ThreatIntelRepository {

    private final FirebaseConfig firebaseConfig;
    private final Firestore firestore;

    // Local in-memory fallback database
    private static final Map<Long, ThreatIntel> localDatabase = new ConcurrentHashMap<>();
    private static long idCounter = 1;

    public ThreatIntelRepository(FirebaseConfig firebaseConfig, java.util.Optional<Firestore> firestore) {
        this.firebaseConfig = firebaseConfig;
        this.firestore = firestore.orElse(null);
    }

    public ThreatIntel save(ThreatIntel threat) {
        if (threat == null) return null;

        if (!firebaseConfig.isFirebaseActive()) {
            if (threat.getId() == null) {
                threat.setId(idCounter++);
            }
            localDatabase.put(threat.getId(), threat);
            return threat;
        }

        try {
            if (threat.getId() == null) {
                threat.setId(System.currentTimeMillis() + (long)(Math.random() * 1000));
            }
            firestore.collection("threats").document(String.valueOf(threat.getId())).set(threat).get();
            return threat;
        } catch (Exception e) {
            System.err.println("Firebase save threat error: " + e.getMessage());
            localDatabase.put(threat.getId(), threat);
            return threat;
        }
    }

    public void saveAll(List<ThreatIntel> threats) {
        if (threats == null) return;
        for (ThreatIntel t : threats) {
            save(t);
        }
    }

    public List<ThreatIntel> findAll() {
        if (!firebaseConfig.isFirebaseActive()) {
            return new ArrayList<>(localDatabase.values());
        }

        List<ThreatIntel> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("threats").get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(ThreatIntel.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findAll threats error: " + e.getMessage());
            return new ArrayList<>(localDatabase.values());
        }
        return list;
    }

    public List<ThreatIntel> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String category, String description) {
        
        // Firestore lacks text search natively. We pull all and filter in memory.
        List<ThreatIntel> all = findAll();
        String qName = name == null ? "" : name.toLowerCase().trim();
        String qCategory = category == null ? "" : category.toLowerCase().trim();
        String qDesc = description == null ? "" : description.toLowerCase().trim();

        return all.stream()
                .filter(t -> (t.getName() != null && t.getName().toLowerCase().contains(qName))
                        || (t.getCategory() != null && t.getCategory().toLowerCase().contains(qCategory))
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(qDesc)))
                .collect(Collectors.toList());
    }

    public long count() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.size();
        }

        try {
            return firestore.collection("threats").get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase count threats error: " + e.getMessage());
            return localDatabase.size();
        }
    }
}

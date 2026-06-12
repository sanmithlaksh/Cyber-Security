package com.cybershield.portal.repository;

import com.cybershield.portal.config.FirebaseConfig;
import com.cybershield.portal.model.QuizQuestion;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class QuizQuestionRepository {

    private final FirebaseConfig firebaseConfig;
    private final Firestore firestore;

    // Local in-memory fallback database
    private static final Map<Long, QuizQuestion> localDatabase = new ConcurrentHashMap<>();
    private static long idCounter = 1;

    public QuizQuestionRepository(FirebaseConfig firebaseConfig, java.util.Optional<Firestore> firestore) {
        this.firebaseConfig = firebaseConfig;
        this.firestore = firestore.orElse(null);
    }

    public QuizQuestion save(QuizQuestion question) {
        if (question == null) return null;

        if (!firebaseConfig.isFirebaseActive()) {
            if (question.getId() == null) {
                question.setId(idCounter++);
            }
            localDatabase.put(question.getId(), question);
            return question;
        }

        try {
            if (question.getId() == null) {
                question.setId(System.currentTimeMillis() + (long)(Math.random() * 1000));
            }
            firestore.collection("quiz_questions").document(String.valueOf(question.getId())).set(question).get();
            return question;
        } catch (Exception e) {
            System.err.println("Firebase save quiz question error: " + e.getMessage());
            localDatabase.put(question.getId(), question);
            return question;
        }
    }

    public void saveAll(List<QuizQuestion> questions) {
        if (questions == null) return;
        for (QuizQuestion q : questions) {
            save(q);
        }
    }

    public List<QuizQuestion> findByCategory(String category) {
        if (category == null) return Collections.emptyList();

        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.values().stream()
                    .filter(q -> category.equalsIgnoreCase(q.getCategory()))
                    .collect(Collectors.toList());
        }

        List<QuizQuestion> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection("quiz_questions")
                    .whereEqualTo("category", category)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                list.add(doc.toObject(QuizQuestion.class));
            }
        } catch (Exception e) {
            System.err.println("Firebase findByCategory quiz question error: " + e.getMessage());
            return localDatabase.values().stream()
                    .filter(q -> category.equalsIgnoreCase(q.getCategory()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public long count() {
        if (!firebaseConfig.isFirebaseActive()) {
            return localDatabase.size();
        }

        try {
            return firestore.collection("quiz_questions").get().get().size();
        } catch (Exception e) {
            System.err.println("Firebase count quiz questions error: " + e.getMessage());
            return localDatabase.size();
        }
    }
}

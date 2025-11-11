package com.example.demo.repository;

import com.example.demo.entity.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, UUID> {
    Optional<ChatbotConversation> findBySessionId(String sessionId);
}

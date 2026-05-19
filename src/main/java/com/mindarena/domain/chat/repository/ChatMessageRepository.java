package com.mindarena.domain.chat.repository;

import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.chat.model.ChatMessage;
import com.mindarena.domain.chat.model.ChatRoomType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select message from ChatMessage message
            where message.roomType = :roomType
            and (:arena is null or message.arena = :arena)
            and (:challenge is null or message.challenge = :challenge)
            order by message.createdAt desc
            """)
    List<ChatMessage> findRoomMessages(
            @Param("roomType") ChatRoomType roomType,
            @Param("arena") Arena arena,
            @Param("challenge") Challenge challenge,
            Pageable pageable
    );
}

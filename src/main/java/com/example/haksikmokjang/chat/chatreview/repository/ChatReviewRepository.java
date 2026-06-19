package com.example.haksikmokjang.chat.chatreview.repository;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatreview.domain.ChatReview;
import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatReviewRepository extends JpaRepository<ChatReview, Long> {

    boolean existsByChatRoomAndReviewerAndTargetMember(ChatRoom chatRoom,
                                                       Member reviewer,
                                                       Member targetMember);

    List<ChatReview> findAllByChatRoomAndReviewer(ChatRoom chatRoom, Member reviewer);

    long countByTargetMemberAndMannerScoreGreaterThanEqual(Member targetMember, Integer mannerScore);

    long countByTargetMemberAndNoShowYn(Member targetMember, String noShowYn);

    long countByTargetMemberAndContentIsNotNull(Member targetMember);
}
package com.example.haksikmokjang.chat.chatroom.repository;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 내가 참여 중인 채팅방 참여 정보 목록 조회
    List<ChatRoomMember> findAllByMember(Member member);

    // 특정 회원이 특정 채팅방에 참여 중인지 확인
    boolean existsByChatRoomAndMember(ChatRoom chatRoom, Member member);

    // 특정 채팅방 안의 특정 회원 참여 정보 조회
    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    // 특정 채팅방의 전체 참여자 조회
    List<ChatRoomMember> findAllByChatRoom(ChatRoom chatRoom);
}
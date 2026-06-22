package com.example.haksikmokjang.chat.chatform.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_FORM_OPTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatFormOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_form_option_id")
    private Long chatFormOptionId;

    // 선택지가 속한 폼
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_form_id", nullable = false)
    private ChatForm chatForm;

    // 선택지를 추가한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id", nullable = false)
    private Member createdByMember;

    // 일반 투표는 선택지 문구, 장소 투표는 장소명 표시용
    @Column(name = "option_text", nullable = false, length = 100)
    private String optionText;

    // 화면 표시 순서
    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    // 장소 후보 출처: 점주 가게 / 직접 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "place_source", length = 20)
    private ChatPlaceSource placeSource;

    // 점주 가게 후보일 때 store_id 저장
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "place_name", length = 100)
    private String placeName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude", columnDefinition = "DECIMAL(10,7)")
    private Double latitude;

    @Column(name = "longitude", columnDefinition = "DECIMAL(10,7)")
    private Double longitude;

    @Column(name = "map_url", length = 500)
    private String mapUrl;

    public ChatFormOption(
            ChatForm chatForm,
            Member createdByMember,
            String optionText,
            int optionOrder,
            ChatPlaceSource placeSource,
            Long storeId,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            String mapUrl
    ) {
        this.chatForm = chatForm;
        this.createdByMember = createdByMember;
        this.optionText = optionText;
        this.optionOrder = optionOrder;
        this.placeSource = placeSource;
        this.storeId = storeId;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mapUrl = mapUrl;
    }
}

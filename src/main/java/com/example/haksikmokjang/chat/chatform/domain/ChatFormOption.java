package com.example.haksikmokjang.chat.chatform.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_FORM_OPTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatFormOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_form_option_id")
    private Long chatFormOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_form_id", nullable = false)
    private ChatForm chatForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id", nullable = false)
    private Member createdByMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", length = 20)
    private ChatFormOptionType optionType;

    @Column(name = "option_text", nullable = false, length = 100)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_source", length = 20)
    private ChatPlaceSource placeSource;

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

    @Column(name = "appointment_at")
    private LocalDateTime appointmentAt;

    @Column(name = "memo", length = 100)
    private String memo;

    public ChatFormOption(
            ChatForm chatForm,
            Member createdByMember,
            ChatFormOptionType optionType,
            String optionText,
            int optionOrder,
            ChatPlaceSource placeSource,
            Long storeId,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            String mapUrl,
            LocalDateTime appointmentAt,
            String memo
    ) {
        this.chatForm = chatForm;
        this.createdByMember = createdByMember;
        this.optionType = optionType;
        this.optionText = optionText;
        this.optionOrder = optionOrder;
        this.placeSource = placeSource;
        this.storeId = storeId;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mapUrl = mapUrl;
        this.appointmentAt = appointmentAt;
        this.memo = memo;
    }
}

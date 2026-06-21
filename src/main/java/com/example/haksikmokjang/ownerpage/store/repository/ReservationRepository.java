package com.example.haksikmokjang.ownerpage.store.repository;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // INNER JOIN FETCH를 사용하여 Reservation과 Member(예약자)를 한 번의 SELECT로 가져옵니다.
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.member m " +
            "WHERE r.store.member.loginId = :ownerLoginId " +
            "ORDER BY r.reservationAt DESC")

    List<Reservation> findAllByOwnerLoginIdWithMember(@Param("ownerLoginId") String ownerLoginId);

    // 유저가 자신의 예약 내역을 볼 때 식당 이름(Store Name)이 필요하므로 Store를 Fetch Join 합니다.
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.store s " +
            "WHERE r.member.loginId = :loginId " +
            "ORDER BY r.reservationAt DESC")

    List<Reservation> findAllByMemberLoginIdWithStore(@Param("loginId") String loginId);
}
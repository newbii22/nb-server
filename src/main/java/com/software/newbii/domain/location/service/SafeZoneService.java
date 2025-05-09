package com.software.newbii.domain.location.service;

import com.software.newbii.domain.member.Member;
import com.software.newbii.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SafeZoneService {
    private final MemberRepository memberRepo;
    private final GeometryFactory gf;

    @Transactional(readOnly = true)
    public boolean isInsideSafeZone(Long memberId, BigDecimal lat, BigDecimal lon) {
        Member m = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원없음"));
        return false;
    }
}

package com.software.newbii.domain.location.service;

import com.software.newbii.domain.location.Location;
import com.software.newbii.domain.location.dto.LocationDto;
import com.software.newbii.domain.location.dto.LocationRequest;
import com.software.newbii.domain.location.dto.LocationResponse;
import com.software.newbii.domain.location.repository.LocationRepository;
import com.software.newbii.domain.member.Member;
import com.software.newbii.domain.member.dto.MemberDto;
import com.software.newbii.domain.member.repository.MemberRepository;
import com.software.newbii.domain.member.service.MemberService;
import com.software.newbii.global.exception.BaseException;
import com.software.newbii.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.software.newbii.domain.location.LocationType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // top 2 frequent
    @Transactional(readOnly = true)
    public List<LocationResponse> getTopFrequent(Long memberId) {
        Member member = loadMember(memberId);
        return locationRepository.findTop2ByMemberAndLocationTypeOrderByVisitCountDesc(member, RECORDED)
                .stream().map(LocationResponse::of).toList();
    }

    // favorites (최대 3)
    @Transactional(readOnly = true)
    public List<LocationResponse> getFavorites(Long memberId) {
        Member member = loadMember(memberId);
        return locationRepository.findByMemberAndLocationType(member, FAVORITE)
                .stream().limit(3).map(LocationResponse::of).toList();
    }

    // home
    @Transactional(readOnly = true)
    public LocationResponse getHome(Long memberId) {
        Member member = loadMember(memberId);
        Location home = locationRepository.findByMemberAndLocationType(member, HOME)
                .orElseThrow(() -> BaseException.from(ErrorCode.LOCATION_NOT_FOUND));
        return LocationResponse.of(home);
    }

//    // 즐겨찾기 토글
//    @Transactional
//    public void toggleFavorite(Long memberId, LocationRequest req) {
//        Member member = loadMember(memberId);
//        Location location = findOrCreate(req, member);
//        location.setLocationType(
//                location.getLocationType() == FAVORITE ? RECORDED : FAVORITE
//        );
//        locationRepository.save(location);
//    }

    // 집 주소 변경
    @Transactional
    public void setHome(Long memberId, BigDecimal latitude, BigDecimal longitude) {
        MemberDto memberDto = memberService.saveMember(memberId);
        Member member = Member.fromDto(memberDto);
        LocationDto locationDto = LocationDto.of(latitude, longitude);
        Location location = saveNewLocation(locationDto, member);
        location.setLocationType(HOME);
        locationRepository.save(location);
    }

    private Member loadMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> BaseException.from(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public String update(LocationRequest request, Long memberId) {

        Member member = loadMember(memberId);

        // 기존 좌표가 존재 하는지
        Optional<Location> location = getLocation(request);

        if (location.isPresent()) {
            // 있으면 방문 횟수 +1, 마지막 방문 시간 갱신
            Location loc = location.get();
            loc.setVisitCount(loc.getVisitCount() + 1);
            loc.setLastVisitedAt(LocalDateTime.now());
            loc.post(member);
            locationRepository.save(loc);

            log.info("기존 위치가 존재합니다");

            return "방문 정보가 업데이트되었습니다.";
        }

        LocationDto dto = LocationDto.of(request.getLatitude(), request.getLongitude());
        // 신규 위치면 등록
        saveNewLocation(dto, member);

        return "등록 되었습니다";
    }

    private Optional<Location> getLocation(LocationRequest request) {
        BigDecimal lat = request.getLatitude();
        BigDecimal lon = request.getLongitude();

        return locationRepository.findByLatitudeAndLongitude(lat, lon);
    }

    private Location saveNewLocation(LocationDto dto, Member member) {
        // 신규 위치면 새로 저장
        Location location = registerLocation(dto, member.getId());
        location.post(member);
        locationRepository.save(location);
        return location;
    }

    private static Location registerLocation(LocationDto locationDto, Long memberId) {
        return Location.builder()
                .latitude(locationDto.getLatitude())
                .longitude(locationDto.getLongitude())
                .locationType(HOME)
                .locationName(HOME.name())
                .visitCount(1)
                .lastVisitedAt(LocalDateTime.now())
                .build();
    }

    private Location loadLocation(Long locationId){
        return locationRepository.findById(locationId)
                .orElseThrow(() -> BaseException.from(ErrorCode.LOCATION_NOT_FOUND));
    }
}
package com.software.newbii.domain.location.controller;

import com.software.newbii.domain.alert.service.AlertService;
import com.software.newbii.domain.location.Location;
import com.software.newbii.domain.location.dto.LocationRequest;
import com.software.newbii.domain.location.dto.LocationResponse;
import com.software.newbii.domain.location.service.LocationService;
//import com.software.newbii.domain.location.service.SafeZoneService;
import com.software.newbii.domain.location.service.SafeZoneService;
import com.software.newbii.domain.location.swagger.PostNewLocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api")
@Tag(name = "위치", description = "위치 api")
public class LocationController {

    private final LocationService locationService;
    private final SafeZoneService safeZoneService;
    private final AlertService alertService;

    @GetMapping("/check-safe")
    public ResponseEntity<Void> checkHomeRadius(
            @RequestParam Long memberId,
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude) {

        boolean inside = safeZoneService.isInsideSafeZone(memberId, latitude, longitude);
        if (!inside) {
            alertService.notifyGuardian(memberId, latitude, longitude);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    // 상위 2개 주기 방문 장소 조회 (RECORDED 중 visitCount 내림차순)
    @GetMapping("/v3/location/frequent")
    public List<LocationResponse> getFrequent(
            @RequestParam Long memberId) {
        return locationService.getTopFrequent(memberId);
    }

    // 즐겨찾기 3곳 조회
    @GetMapping("/v3/location/favorites")
    public List<LocationResponse> getFavorites(
            @RequestParam Long memberId) {
        return locationService.getFavorites(memberId);
    }

    @GetMapping("/v3/location/home")
    public LocationResponse getHome(
            @RequestParam Long memberId) {
        return locationService.getHome(memberId);
    }

//    // 즐겨찾기 추가/삭제
//    @PostMapping("/favorites")
//    public void toggleFavorite(
//            @RequestParam Long memberId,
//            @RequestBody LocationRequest req) {
//        locationService.toggleFavorite(memberId, req);
//    }

    @PostMapping("/v3/location/home")
    public String setHome(
            @RequestParam Long memberId,
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude) {
        locationService.setHome(memberId, latitude, longitude);
        return "등록되었습니다.";
    }

    @PostNewLocation
    @PostMapping("/v3/location")
    public String updateLocation(@RequestBody LocationRequest request, Long memberId) {
        return locationService.update(request, memberId);
    }

//    /** 매 20분마다 Flutter → 이 엔드포인트 호출 */
//    @PostMapping("/check-safe")
//    public ResponseEntity<Void> checkSafe(
//            @RequestParam Long memberId,
//            @RequestParam double latitude,
//            @RequestParam double longitude) {
//
//        boolean safe = safeZoneService.isInsideSafeZone(memberId, latitude, longitude);
//        if (!safe) {
//            alertService.notifyGuardian(memberId, latitude, longitude);
//            return ResponseEntity.status(403).build();
//        }
//        return ResponseEntity.ok().build();
//    }


}

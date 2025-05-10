package com.software.newbii.domain.location;


import com.software.newbii.domain.location.dto.LocationRequest;
import com.software.newbii.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

  //위도
  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal latitude;

  //경도
  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal longitude;

  private String locationName;

  @Column(nullable = false)
  private LocationType locationType;

  private Integer visitCount;

  private LocalDateTime lastVisitedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Builder
  public Location(BigDecimal latitude,
                  BigDecimal longitude,
                  String locationName,
                  LocationType locationType,
                  Integer visitCount,
                  LocalDateTime lastVisitedAt) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationName = locationName;
    this.locationType = locationType;
    this.visitCount= visitCount;
    this.lastVisitedAt = lastVisitedAt;
  }

  public void post(Member member){
    this.member = member;
    //member.getLocations().add(this);
  }

  public static Location from(LocationRequest request){
    return Location.builder()
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .locationName(request.getLocationName())
            .locationType(request.getLocationType())
            .visitCount(request.getVisitCount())
            .lastVisitedAt(request.getLastVisitedAt())
            .build();
  }

}


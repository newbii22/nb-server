package com.software.newbii.domain.member;

import com.software.newbii.domain.location.Location;
import com.software.newbii.domain.member.dto.MemberDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.MultiPolygon;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable= false, unique = true)
    private String email;

    @Column(nullable= false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable= false)
    private MemberRole role;

    @Column(nullable= false)
    private String name;

    private LocalDate birth;

    private String phone;
    
    private String oauthId;
  
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private Member guardian;


    @OneToOne(mappedBy = "guardian", fetch = FetchType.LAZY)
    private Member user;

    public void assignUser(Member user) {
        if (this.role != MemberRole.GUARDIAN) {
            throw new IllegalStateException("피보호자를 지정할 수 있는 권한이 없습니다.");
        }
        this.user = user;
        user.guardian = this;
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();


    @Builder
    public Member(String email, String password, MemberRole role, String name,
                  LocalDate birth, String phone, String oauthId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.birth = birth;
        this.phone = phone;
        this.oauthId = oauthId;
    }

    public static Member fromDto(MemberDto dto) {
        return Member.builder()
                .email(dto.getEmail())
                .password("1234")
                .role(dto.getRole())
                .name(dto.getName())
                .birth(dto.getBirth())
                .phone(dto.getPhone())
                .oauthId("dto.getOauthId()")
                .build();
    }
}

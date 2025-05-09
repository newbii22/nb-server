package com.software.newbii.domain.member.service;

import com.software.newbii.domain.member.Member;
import com.software.newbii.domain.member.dto.MemberCommand;
import com.software.newbii.domain.member.MemberRole;
import com.software.newbii.domain.member.dto.MemberDto;
import com.software.newbii.domain.member.repository.MemberRepository;
import com.software.newbii.global.exception.BaseException;
import com.software.newbii.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
  // update
  // delete
  private final MemberRepository memberRepository;

  // create
  @Transactional
  public MemberDto createMember(MemberCommand command) {
    validateEmailNotExists(command.getEmail());

    Member member = Member.builder()
        .email(command.getEmail())
        .password(command.getPassword()) // TODO: 비밀번호 암호화 필요
        .name(command.getName())
        .phone(command.getPhone())
        .birth(command.getBirth())
        .role(command.getRole())
        .build();

    Member saved = memberRepository.save(member);
    log.info("회원 생성 성공: {}", saved.getId());

    return MemberDto.builder()
        .id(saved.getId())
        .email(saved.getEmail())
        .name(saved.getName())
        .phone(saved.getPhone())
        .birth(saved.getBirth())
        .role(saved.getRole())
        .build();
  }


    @Transactional
    public MemberDto saveMember(Long memberId) {

        Member member = Member.builder()
                .id(memberId)
                .email("nb@gmail.com")
                .password("1234") // TODO: 비밀번호 암호화 필요
                .name("NB")
                .phone("010-1111-2222")
                .birth(LocalDate.parse("03-05-30"))
                .role(MemberRole.USER)
                .build();

        Member saved = memberRepository.save(member);
        log.info("회원 생성 성공: {}", saved.getId());

        return MemberDto.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .phone(saved.getPhone())
                .birth(saved.getBirth())
                .role(saved.getRole())
                .build();
    }

  // read
  @Transactional(readOnly = true)
  public MemberDto getMemberInfo(MemberCommand memberCommand) {
    Member member = memberRepository.findById(memberCommand.getId())
        .orElseThrow(() -> {
          log.error("회원 조회 실패: memberId: {}", memberCommand.getId());
          return BaseException.from(ErrorCode.MEMBER_NOT_FOUND);
        });

    if (member.getRole() == MemberRole.GUARDIAN) {
      return MemberDto.builder()
          .id(member.getId())
          .email(member.getEmail())
          .role(member.getRole())
          .name(member.getName())
          .birth(member.getBirth())
          .phone(member.getPhone())
          .userId(member.getUser() != null ? member.getUser().getId() : null) // 보호자가 보는 피보호자
          .build();
    } else {
      return MemberDto.builder()
          .id(member.getId())
          .email(member.getEmail())
          .role(member.getRole())
          .name(member.getName())
          .birth(member.getBirth())
          .phone(member.getPhone())
          .guardianId(member.getGuardian() != null ? member.getGuardian().getId() : null) // 피보호자가 보는 보호자
          .build();
    }
  }

  // update
  @Transactional
  public void updateMember(MemberCommand command) {
    Member member = memberRepository.findById(command.getId())
        .orElseThrow(() -> {
          log.error("회원 수정 실패: memberId: {}", command.getId());
          return BaseException.from(ErrorCode.MEMBER_NOT_FOUND);
        });

    member.setName(command.getName());
    member.setPhone(command.getPhone());
    member.setBirth(command.getBirth());

    // 보호자인 경우에만 userId 설정
    if (member.getRole() == MemberRole.GUARDIAN && command.getUserId() != null) {
      Member user = memberRepository.findById(command.getUserId())
          .orElseThrow(() -> {
            log.error("피보호자 조회 실패: userId: {}", command.getUserId());
            return BaseException.from(ErrorCode.MEMBER_NOT_FOUND);
          });

      member.assignUser(user);
    }

    log.info("회원 수정 성공: memberId: {}", member.getId());
  }

  // delete (하드 삭제)
  @Transactional
  public void deleteMember(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 삭제 실패: memberId: {}", memberId);
          return BaseException.from(ErrorCode.MEMBER_NOT_FOUND);
        });

    memberRepository.delete(member);
    log.info("회원 삭제 성공 (하드 삭제): memberId: {}", memberId);
  }

  private void validateEmailNotExists(String email) {
    if (memberRepository.existsByEmail(email)) {
      log.error("이메일 중복: {}", email);
      throw BaseException.from(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }



}

package study.querydsl.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberSearchCondition;

@RestController
@RequiredArgsConstructor
public class MemberController {

  private final MemberJpaRepository memberJpaRepository;

  @GetMapping("/v1/members")
  public List<MemberTeamDto> searchMemberV1(@RequestBody MemberSearchCondition condition) {
    return memberJpaRepository.search(condition);
  }
}
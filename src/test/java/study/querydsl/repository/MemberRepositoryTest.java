package study.querydsl.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
  
    @Autowired
    MemberRepository memberRepository;
    
    @Autowired
    EntityManager em;
    
    @Test
    public void check() throws Exception {
      //given
      Team teamA = new Team("teamA");
      Member seok = new Member("seok", 30, teamA);
      em.persist(teamA);
      em.persist(seok);
      
      em.flush();
      em.clear();
      //when
      List<Member> memberJoinTeam = memberRepository.findMemberJoinTeam("teamA");
      for (Member member : memberJoinTeam) {
        System.out.println("member. = " + member.getTeam().getClass());
        System.out.println("체크"+member.getTeam().getName());
      }
      //then
    }
    
    
}
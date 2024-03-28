package study.querydsl;

import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired
  EntityManager em;

  JPQLQueryFactory queryFactory;

  @BeforeEach
  public void before(){
    queryFactory = new JPAQueryFactory(em);
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);
    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);
    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);
  }

  @Test
  public void startJPQL() throws Exception {
    //given
    String qlString =
            "select m from Member m " +
            "where m.username = :username";
    Member findMember = em.createQuery(qlString, Member.class)
        .setParameter("username", "member1")
        .getSingleResult();
    //when
    
    //then
    assertEquals(findMember.getUsername(),"member1");
  }
  
  @Test
  public void startQuerydsl() throws Exception {
    //given

    //when
    Member findMember = queryFactory
        .select( member)
        .from(member)
        .where(member.username.eq("member1"))
        .fetchOne();
    //then
    assertEquals(findMember.getUsername(),"member1");
  }

  @Test
  public void search() throws Exception {
    //given
    List<Member> findMember = queryFactory
        .selectFrom(member)
        .where(
            member.username.eq("member1"),
            member.age.eq(10).or(
            member.age.eq(20)
            )
        )
        .fetch();
    //when
    for (Member member1 : findMember) {
      System.out.println("member1.getUsername() = " + member1.getUsername());
    }
    //then

  }

  @Test
  public void searchAndParam() throws Exception {
    //given
    Member findMember = queryFactory
        .selectFrom(member)
        .where(
            member.username.eq("member1"),
            member.age.eq(10)
        )
        .fetchOne();
    //when

    //then
    assertEquals(findMember.getUsername(),"member1");
  }

  @Test
  public void resultFetch() throws Exception {
    //given
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .fetch();
    System.out.println("===============================================");
//    Member fetchOne = queryFactory
//        .selectFrom(member)
//        .fetchOne();
    System.out.println("===============================================");
    Member fetchFirst = queryFactory
        .selectFrom(member)
        .fetchFirst();
    System.out.println("===============================================");
    QueryResults<Member> results = queryFactory
        .selectFrom(member)
        .fetchResults();

    System.out.println("체크"+results.getTotal());
    List<Member> content = results.getResults();
    System.out.println("===============================================");
    long total = queryFactory
        .selectFrom(member)
        .fetchCount();
    System.out.println("===============================================");
    //when

    //then
  }

  // 회원 나이 내림차순 , 회원 이름 오름차순
  // 단 회원 이름이 없으면 마지막에 출력(nulls last)
  @Test
  public void sort() throws Exception {
    //given
    em.persist(new Member(null,100));
    em.persist(new Member("member5",100));
    em.persist(new Member("member6",100));

    List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.eq(100))
        .orderBy(member.age.desc(), member.username.asc().nullsLast())
        .fetch();
//    for (Member member1 : result) {
//      System.out.println("member1 = " + member1);
//    }
    Member member5 = result.get(0);
    Member member6 = result.get(1);
    Member memberNull = result.get(2);
    assertEquals(member5.getUsername(),"member5");
    assertEquals(member6.getUsername(),"member6");
    assertNull(memberNull.getUsername());
  }
  
  




}

package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
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
  public void before() {
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
    assertEquals(findMember.getUsername(), "member1");
  }

  @Test
  public void startQuerydsl() throws Exception {
    //given

    //when
    Member findMember = queryFactory
        .select(member)
        .from(member)
        .where(member.username.eq("member1"))
        .fetchOne();
    //then
    assertEquals(findMember.getUsername(), "member1");
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
    assertEquals(findMember.getUsername(), "member1");
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

    System.out.println("체크" + results.getTotal());
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
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

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
    assertEquals(member5.getUsername(), "member5");
    assertEquals(member6.getUsername(), "member6");
    assertNull(memberNull.getUsername());
  }

  @Test
  public void paging1() throws Exception {
    //given
    QueryResults<Member> queryResults = queryFactory
        .selectFrom(member)
        .orderBy(member.age.desc())
        .offset(1)
        .limit(2)
        .fetchResults();

    assertEquals(queryResults.getTotal(), 4);
    assertEquals(queryResults.getLimit(), 2);
    assertEquals(queryResults.getOffset(), 1);
    assertEquals(queryResults.getResults().size(), 2);
    //when

    //then
  }

  @Test
  public void aggregation() throws Exception {
    //given
    List<Tuple> result = queryFactory
        .select(
            member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min()
        )
        .from(member)
        .fetch();

    Tuple tuple = result.get(0);
    assertEquals(tuple.get(member.count()), 4);
    assertEquals(tuple.get(member.age.sum()), 100);
    assertEquals(tuple.get(member.age.avg()), 25);
    assertEquals(tuple.get(member.age.max()), 40);
    assertEquals(tuple.get(member.age.min()), 10);
    //when

    //then
  }

  @Test
  public void group() throws Exception {
    //given
    List<Tuple> result = queryFactory
        .select(team.name, member.age.avg())
        .from(member)
        .join(member.team, team)
        .groupBy(team.name)
        .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);
    assertEquals(teamA.get(team.name), "teamA");
    assertEquals(teamA.get(member.age.avg()), 15);
    assertEquals(teamB.get(team.name), "teamB");
    assertEquals(teamB.get(member.age.avg()), 35);
  }

  @Test
  public void join() throws Exception {
    //given
    List<Member> result = queryFactory
        .selectFrom(member)
        .join(member.team, team)
        .where(team.name.eq("teamA"))
        .fetch();

    //when

    //then
  }

  //예) 회원과 팀을 조인하면서,팀 이름이 teamA인 팀만 조인,회원은 모두 조회
  // JPQL의 경우: select m, t from Member m left join m.team t on t.name = 'teamA'
  @Test
  public void join_on_filtering() throws Exception {
    //given
    List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(member.team, team).on(team.name.eq("teamA"))
        .fetch();
    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
    //when

    //then
  }


  // 2. 연관관계 없는 엔티티 외부 조인
  // 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
  // JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
  // SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
  @Test
  public void join_on_no_relation() throws Exception {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(team).on(member.username.eq(team.name))
        .fetch();
    for (Tuple tuple : result) {
      System.out.println("t=" + tuple);
    }
  }

  @PersistenceUnit
  EntityManagerFactory emf;

  @Test
  public void fetchJoinNo() throws Exception {
    //given
    em.flush();
    em.clear();

    Member fondMember = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fondMember.getTeam());
    assertFalse(loaded);
    //when

    //then
  }

  @Test
  public void fetchJoinUse() throws Exception {
    //given
    em.flush();
    em.clear();

    Member fondMember = queryFactory
        .selectFrom(member)
        .join(member.team, team).fetchJoin()
        .where(member.username.eq("member1"))
        .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fondMember.getTeam());
    System.out.println("체크" + fondMember.getTeam().getClass());
    assertTrue(loaded);
  }

  // 서브쿼리: 나이가 가장 많은 회원 조회
  @Test
  @DisplayName("")
  public void subQuery() throws Exception {
    //given
    QMember memberSub = new QMember("memberSub");

    Member findMember = queryFactory
        .selectFrom(member)
        .where(member.age.eq(
            JPAExpressions
                .select(memberSub.age.max())
                .from(memberSub)
        ))
        .fetchOne();
    assertEquals(findMember.getAge(), 40);
  }

  // 서브쿼리: 나이가 평균 이상인 회원
  @Test
  @DisplayName("")
  public void subQueryGoe() throws Exception {
    //given
    QMember memberSub = new QMember("memberSub");

    List<Member> findMember = queryFactory
        .selectFrom(member)
        .where(member.age.goe(
            JPAExpressions
                .select(memberSub.age.avg())
                .from(memberSub)
        ))
        .fetch();

    assertEquals(findMember.size(), 2);
    assertThat(findMember).extracting("age").containsExactly(30, 40);
  }

  // 서브쿼리: 10살 초과 회원
  @Test
  @DisplayName("")
  public void subQueryIn() throws Exception {
    //given
    QMember memberSub = new QMember("memberSub");

    List<Member> findMember = queryFactory
        .selectFrom(member)
        .where(member.age.in(
            JPAExpressions
                .select(memberSub.age)
                .from(memberSub)
                .where(memberSub.age.gt(10))
        ))
        .fetch();

    assertEquals(findMember.size(), 3);
    assertThat(findMember).extracting("age").containsExactly(20, 30, 40);
  }

  @Test
  public void basicCase() throws Exception {
    List<String> result = queryFactory
        .select(member.age
            .when(10).then("열살")
            .when(20).then("스무살")
            .otherwise("기타"))
        .from(member)
        .fetch();
  }

  @Test
  public void buildCase() throws Exception {
    List<String> result = queryFactory
        .select(new CaseBuilder()
            .when(member.age.between(0, 20)).then("0~20살")
            .when(member.age.between(21, 30)).then("21~30살")
            .otherwise("기타"))
        .from(member)
        .fetch();
  }

  @Test
  @DisplayName("")
  public void constant() throws Exception {
    List<Tuple> result = queryFactory
        .select(member.username, Expressions.constant("A"))
        .from(member)
        .fetch();
    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }

  }

  @Test
  public void concat() throws Exception {
    String result = queryFactory
        .select(member.username.concat("_").concat(member.age.stringValue()))
        .from(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    System.out.println("result = " + result);

  }

  @Test
  public void findDtoByJPQL() throws Exception {
    List<MemberDto> result = em.createQuery(
            "select new study.querydsl.dto.MemberDto(m.username, m.age) " +
                "from Member m", MemberDto.class)
        .getResultList();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }
  
  @Test
  @DisplayName("")
  public void findDtoBySetter() throws Exception {
    List<MemberDto> result = queryFactory
        .select(Projections.bean(MemberDto.class,
            member.username,
            member.age))
        .from(member)
        .fetch();
    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }
  @Test
  @DisplayName("")
  public void findDtoByField() throws Exception {
    List<MemberDto> result = queryFactory
        .select(Projections.fields(MemberDto.class,
            member.username,
            member.age))
        .from(member)
        .fetch();
    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }

  @Test
  @DisplayName("")
  public void findDtoByConstructor() throws Exception {
    List<MemberDto> result = queryFactory
        .select(Projections.constructor(MemberDto.class,
            member.username,
            member.age))
        .from(member)
        .fetch();
    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }
  
  @Test
  public void findDtoByQueryProjection() throws Exception {
    List<MemberDto> result = queryFactory
        .select(new QMemberDto(member.username, member.age))
        .from(member)
        .fetch();
    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }
  }

  @Test
  public void dynamicQuery_BooleanBuilder() throws Exception {
    String usernameParam = null;
    Integer ageParam = null;
    List<Member> result = searchMember1(usernameParam,ageParam);
    for (Member member1 : result) {
      System.out.println("member1 = " + member1);
    }

  }

  private List<Member> searchMember1(String usernameCond, Integer ageCond) {
    BooleanBuilder builder = new BooleanBuilder();
    if(usernameCond != null){
      builder.and(member.username.eq(usernameCond));
    }

    if(ageCond != null){
      builder.and(member.age.eq(ageCond));
    }
    return queryFactory
        .selectFrom(member)
        .where(builder)
        .fetch();

  }

  
  





}

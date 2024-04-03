package study.querydsl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>,MemberRepositoryCustom,
    QuerydslPredicateExecutor<Member> {

  @Query("select m from Member m join m.team t where t.name = :name")
  List<Member> findMemberJoinTeam(@Param("name") String name);

  List<Member> findByUsername(String username);
}

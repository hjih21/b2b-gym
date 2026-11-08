package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.member.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class MemberRepositoryTest {
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("회원을 저장하고 id로 조회하면 저장한 값이 나온다")
    void saveAndFindById(){
        // given
        Member member = Member.createMember("member@example.com", "abc123", "테스터", "01012345678");
        memberRepository.save(member);
        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findById(member.getId()).orElseThrow();

        // then
        assertThat(findMember.getEmail()).isEqualTo("member@example.com");
        assertThat(findMember.getPhone()).isEqualTo("01012345678");
        assertThat(findMember.getCreatedAt()).isNotNull();
    }
}
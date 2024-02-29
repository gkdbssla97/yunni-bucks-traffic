package sejong.coffee.yun.unit.repository.user.fake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.exception.DuplicatedEmailException;
import sejong.coffee.yun.domain.exception.DuplicatedNameException;
import sejong.coffee.yun.domain.user.Address;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.domain.user.UserRank;
import sejong.coffee.yun.mock.repository.FakeUserRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sejong.coffee.yun.domain.exception.ExceptionControl.DUPLICATE_USER_EMAIL;
import static sejong.coffee.yun.domain.exception.ExceptionControl.DUPLICATE_USER_NAME;

class FakeMemberRepositoryTest {

    private final UserRepository userRepository;

    public FakeMemberRepositoryTest() {
        this.userRepository = new FakeUserRepository();
    }

    private Member member;


    @BeforeEach
    void init() {
        String city = "서울시";
        String district = "강서구";
        String detail = "목동";
        String zipcode = "123-123";

        Address address = new Address(city, district, detail, zipcode);

        member = Member.builder()
                .address(address)
                .email("qwer1234@naver.com")
                .money(Money.ZERO)
                .userRank(UserRank.BRONZE)
                .password("qwer1234")
                .name("윤광오")
                .build();
    }

    @Test
    void 유저_저장() {
        // given

        // when
        Member save = userRepository.save(member);

        // then
        assertThat(save.getId()).isEqualTo(1);
    }

    @Test
    void 유저_이름_변경() {
        // given
        String updateName = "하윤";
        Member save = userRepository.save(member);

        // when
        userRepository.updateName(save.getId(), updateName);

        // then
        assertThat(save.getName()).isEqualTo(updateName);
    }

    @Test
    void 유저가_존재하는지_확인() {
        // given
        Member save = userRepository.save(member);

        // when
        boolean exist = userRepository.exist(save.getId());

        // then
        assertTrue(exist);
    }

    @Test
    void 유저_이메일이_중복_되는지_확인() {
        // given
        Member save = userRepository.save(member);

        // when

        // then
        assertThatThrownBy(() -> userRepository.duplicateEmail(save.getEmail()))
                .isInstanceOf(DuplicatedEmailException.class)
                .hasMessageContaining(DUPLICATE_USER_EMAIL.getMessage());
    }

    @Test
    void 유저_이름이_중복_되는지_확인() {
        // given
        Member save = userRepository.save(member);

        // when

        // then
        assertThatThrownBy(() -> userRepository.duplicateName(save.getName()))
                .isInstanceOf(DuplicatedNameException.class)
                .hasMessageContaining(DUPLICATE_USER_NAME.getMessage());
    }
}
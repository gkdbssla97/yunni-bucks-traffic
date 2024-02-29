package sejong.coffee.yun.unit.domain.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.domain.user.UserRank;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRankTest {

    @ParameterizedTest
    @CsvSource({"0, BRONZE", "1, SILVER", "6, GOLD", "11, PLATINUM", "16, DIAMOND"})
    void 주문_개수_기준으로_등급_승급(int orderCount, UserRank rank) {
        // given
        Member member = Member.builder()
                .userRank(UserRank.calculateUserRank(orderCount))
                .build();

        // when
        UserRank userRank = member.getUserRank();

        // then
        assertThat(userRank).isEqualTo(rank);
    }

    @ParameterizedTest
    @CsvSource({"BRONZE, 0.0", "SILVER, 0.1", "GOLD, 0.15", "PLATINUM, 0.2", "DIAMOND, 0.3"})
    void 유저_등급에_따른_할인률(UserRank userRank, double discountRate) {

        assertThat(userRank.getDiscountRate()).isEqualTo(discountRate);
    }
}
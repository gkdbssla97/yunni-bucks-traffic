package sejong.coffee.yun.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.DateTimeEntity;
import sejong.coffee.yun.util.password.PasswordUtil;

import javax.persistence.*;
import java.math.BigDecimal;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_MATCH_USER;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends DateTimeEntity {

    @Id @GeneratedValue
    private Long id;
    @Column(name = "user_name")
    private String name;
    private String password;
    @Column(name = "email")
    private String email;
    @Enumerated(value = EnumType.STRING)
    private UserRank userRank;
    private Address address;
    private Money money;
    @Column(name = "order_count")
    private Integer orderCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Version
    private Long version;

    @Builder
    public Member(Long id, String name, String password, String email, UserRank userRank, Address address, Money money, Integer orderCount, Coupon coupon) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.userRank = userRank;
        this.address = address;
        this.money = money;
        this.orderCount = orderCount;
        this.coupon = coupon;
    }

    public static Member from(Long id, Member member) {
        return Member.builder()
                .id(id)
                .name(member.getName())
                .password(member.getPassword())
                .orderCount(member.getOrderCount())
                .email(member.getEmail())
                .money(member.getMoney())
                .userRank(member.getUserRank())
                .address(member.getAddress())
                .coupon(member.getCoupon())
                .build();
    }

    public void upgradeUserRank(int orderCount) {
        this.userRank = UserRank.calculateUserRank(orderCount);
    }

    public void addOrderCount() {
        this.orderCount++;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = PasswordUtil.encryptPassword(password);
    }

    public BigDecimal fetchTotalPrice() {
        return this.money.getTotalPrice();
    }
    public boolean hasCoupon() {
        return this.coupon != null && this.coupon.hasAvailableCoupon();
    }
    public void checkPasswordMatch(String checkPassword) {
        boolean match = PasswordUtil.match(this.password, checkPassword);

        if(!match) throw NOT_MATCH_USER.notMatchUserException();
    }
}

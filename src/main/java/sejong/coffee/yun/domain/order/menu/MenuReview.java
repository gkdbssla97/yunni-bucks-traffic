package sejong.coffee.yun.domain.order.menu;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.user.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menu_review")
public class MenuReview {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;
    @Column(name = "create_at")
    private LocalDateTime createAt;
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Builder
    public MenuReview(Long id, String comments, Member member, Menu menu, LocalDateTime now) {
        this.id = id;
        this.comments = comments;
        this.member = member;
        this.menu = menu;
        this.createAt = now;
        this.updateAt = now;
    }

    public static MenuReview create(String comments, Member member, Menu menu, LocalDateTime now) {
        return MenuReview.builder()
                .comments(comments)
                .member(member)
                .menu(menu)
                .now(now)
                .build();
    }

    public static MenuReview from(Long id, MenuReview menuReview) {
        return MenuReview.builder()
                .id(id)
                .comments(menuReview.getComments())
                .member(menuReview.getMember())
                .menu(menuReview.getMenu())
                .now(menuReview.getCreateAt())
                .build();
    }

    public void updateComment(String comments) {
        this.comments = comments;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}

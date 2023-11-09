package sejong.coffee.yun.domain.order.menu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.user.Money;

import javax.persistence.*;
import java.time.LocalDateTime;

import static sejong.coffee.yun.domain.order.menu.MenuStatus.ON_SALE;
import static sejong.coffee.yun.domain.order.menu.MenuStatus.SOLD_OUT;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DiscriminatorColumn
public abstract class Menu {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private Money price;
    private Nutrients nutrients;
    @Enumerated(value = EnumType.STRING)
    private MenuSize menuSize;
    @Column(name = "create_at")
    private LocalDateTime createAt;
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    @Column(name = "stock")
    private int stock;
    @Enumerated(value = EnumType.STRING)
    private MenuType menuType;
    @Enumerated(value = EnumType.STRING)
    private MenuStatus menuStatus;
    private int orderCount;
    private int viewCount;

    @Version
    private Long version;

    @Transient
    private final int MAX_STOCK = 100;

    protected Menu(Long id, String title, String description, Money price, Nutrients nutrients, MenuSize menuSize, LocalDateTime now, int stock, MenuType menuType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.nutrients = nutrients;
        this.menuSize = menuSize;
        this.createAt = now;
        this.updateAt = now;
        this.stock = stock;
        this.menuType = menuType;
        this.menuStatus = ON_SALE;
    }

    protected Menu(String title, String description, Money price, Nutrients nutrients, MenuSize menuSize, LocalDateTime now) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.nutrients = nutrients;
        this.menuSize = menuSize;
        this.createAt = now;
        this.updateAt = now;
    }

    protected Menu(String title, String description, Money price, Nutrients nutrients, MenuSize menuSize, LocalDateTime now, int stock, MenuType menuType) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.nutrients = nutrients;
        this.menuSize = menuSize;
        this.createAt = now;
        this.updateAt = now;
        this.stock = stock;
        this.menuType = menuType;
        this.menuStatus = ON_SALE;
    }

    public void setUpdateAt(LocalDateTime now) {
        this.updateAt = now;
    }

    public void decrease(int quantity) {
        if(this.stock - quantity == 0) {
            soldOut();
        }
        if (this.stock - quantity < 0) {
            throw ExceptionControl.INSUFFICIENT_STOCK_QUANTITY.menuException();
        }
        this.stock -= quantity;
    }

    public void increaseOrderCount(int quantity) {
        this.orderCount += quantity;
    }

    public void increaseViewCount() {
        this.viewCount += 1;
    }

    public void refillStock() {
        this.stock = MAX_STOCK;
        this.menuStatus = ON_SALE;
    }

    public void soldOut() {
        this.menuStatus = SOLD_OUT;
    }
}

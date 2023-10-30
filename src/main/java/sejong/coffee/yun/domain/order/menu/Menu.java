package sejong.coffee.yun.domain.order.menu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.user.Money;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DiscriminatorColumn
public abstract class Menu {

    @Id @GeneratedValue
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

    protected Menu(Long id, String title, String description, Money price, Nutrients nutrients, MenuSize menuSize, LocalDateTime now, int stock) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.nutrients = nutrients;
        this.menuSize = menuSize;
        this.createAt = now;
        this.updateAt = now;
        this.stock = stock;
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

    protected Menu(String title, String description, Money price, Nutrients nutrients, MenuSize menuSize, LocalDateTime now, int stock) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.nutrients = nutrients;
        this.menuSize = menuSize;
        this.createAt = now;
        this.updateAt = now;
        this.stock = stock;
    }

    public void setUpdateAt(LocalDateTime now) {
        this.updateAt = now;
    }

    public void decrease(int qty) {
        if(this.stock - qty < 0 ){
            throw new RuntimeException();
        }
        this.stock -= qty;
    }
}

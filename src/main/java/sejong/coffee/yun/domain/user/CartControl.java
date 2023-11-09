package sejong.coffee.yun.domain.user;

import lombok.Getter;

@Getter
public enum CartControl {
    SIZE(10);

    private final int size;
    CartControl(int size) {
        this.size = size;
    }
}

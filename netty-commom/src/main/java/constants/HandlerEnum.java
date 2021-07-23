package constants;

import lombok.Getter;

/**
 * TODO
 *
 * @author: dayun_wang
 * @date: 2021-07-23 14:49
 **/
@Getter
public enum HandlerEnum {
    HEART("0")
    ;

    private String type;

    HandlerEnum(String type) {
        this.type = type;
    }
}

package constants;

import lombok.Getter;

@Getter
public enum BoardParameters {

    DESCRIPTION("desc"),
    NAME("name"),
    ID("id"),
    CLOSED("closed"),
    DEFAULT_LISTS("defaultLists");

    private final String parameterName;

    BoardParameters(String parameterName) {
        this.parameterName = parameterName;
    }
}

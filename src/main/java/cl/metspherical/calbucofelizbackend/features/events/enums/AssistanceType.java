package cl.metspherical.calbucofelizbackend.features.events.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AssistanceType {
    ASISTIRE("Asistiré"),
    NO_ASISTIRE("No asistiré"),
    TAL_VEZ("Tal vez");

    private final String displayName;

    AssistanceType(String displayName) {
        this.displayName = displayName;
    }

    public static Optional<AssistanceType> fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equals(displayName))
                .findFirst();
    }
}

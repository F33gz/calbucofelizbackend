package cl.metspherical.calbucofelizbackend.features.events.enums;

import lombok.Getter;

@Getter
public enum AssistanceType {
    ASISTIRE("Asistiré"),
    NO_ASISTIRE("No asistiré"),
    TAL_VEZ("Tal vez");

    private final String displayName;

    AssistanceType(String displayName) {
        this.displayName = displayName;
    }

    public static AssistanceType fromDisplayName(String displayName) {
        for (AssistanceType type : AssistanceType.values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown assistance type: " + displayName);
    }
}

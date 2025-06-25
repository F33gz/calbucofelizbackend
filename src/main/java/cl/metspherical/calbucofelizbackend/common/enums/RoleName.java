package cl.metspherical.calbucofelizbackend.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Enum that defines the available roles in the system
 * Roles are ordered by hierarchy: COMITE_SEGURIDAD > LIDER > MODERADOR
 */
public enum RoleName {
    COMITE_SEGURIDAD(3),
    LIDER(2), 
    MODERADOR(1);
    
    private final int hierarchy;
      RoleName(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public int getHierarchy() {
        return hierarchy;
    }    public boolean canModerate() {
        return hierarchy > 0;
    }

    public static List<RoleName> getModerationRoles() {
        return Arrays.stream(values())
                .filter(RoleName::canModerate)
                .sorted((a, b) -> Integer.compare(b.hierarchy, a.hierarchy))
                .toList();
    }
}

package edu.univ.eventos.security;

import edu.univ.eventos.model.Role;
import io.javalin.http.Context;

import java.util.Set;

public class SecurityUtil {
    public static Long userId(Context ctx) {
        Long id = ctx.sessionAttribute("userId");
        if (id == null) throw new RuntimeException("No autenticado");
        return id;
    }

    public static Role role(Context ctx) {
        String role = ctx.sessionAttribute("role");
        if (role == null) throw new RuntimeException("No autenticado");
        return Role.valueOf(role);
    }

    public static void requireRoles(Context ctx, Set<Role> allowed) {
        Role role = role(ctx);
        if (!allowed.contains(role)) {
            throw new RuntimeException("No autorizado");
        }
    }
}

package edu.univ.eventos.controller;

import edu.univ.eventos.dto.*;
import edu.univ.eventos.model.*;
import edu.univ.eventos.repository.UserRepository;
import edu.univ.eventos.security.SecurityUtil;
import edu.univ.eventos.service.*;
import io.javalin.Javalin;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class ApiController {
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();
    private final EventService eventService = new EventService();
    private final RegistrationService registrationService = new RegistrationService();
    private final UserRepository userRepository = new UserRepository();

    public void register(Javalin app) {
        app.post("/api/auth/login", ctx -> {
            LoginRequest body = ctx.bodyAsClass(LoginRequest.class);
            User user = authService.login(body.email, body.password);
            ctx.sessionAttribute("userId", user.getId());
            ctx.sessionAttribute("role", user.getRole().name());
            ctx.json(Map.of("message", "ok", "role", user.getRole(), "name", user.getName()));
        });

        app.post("/api/auth/register", ctx -> {
            RegisterRequest body = ctx.bodyAsClass(RegisterRequest.class);
            User user = authService.registerParticipant(body.name, body.email, body.password);
            ctx.json(Map.of("id", user.getId(), "email", user.getEmail()));
        });

        app.post("/api/auth/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.json(Map.of("message", "logout"));
        });

        app.get("/api/me", ctx -> {
            Long userId = ctx.sessionAttribute("userId");
            if (userId == null) { ctx.status(401).json(Map.of("authenticated", false)); return; }
            User u = userRepository.findById(userId).orElseThrow(() -> new AppException("Usuario no encontrado"));
            ctx.json(Map.of("authenticated", true, "id", u.getId(), "name", u.getName(), "role", u.getRole()));
        });

        app.get("/api/eventos", ctx -> {
            boolean all = "true".equals(ctx.queryParam("all"));
            ctx.json(all ? eventService.all() : eventService.published());
        });

        app.post("/api/eventos", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN, Role.ORGANIZADOR));
            EventRequest req = ctx.bodyAsClass(EventRequest.class);
            User creator = userRepository.findById(SecurityUtil.userId(ctx)).orElseThrow();
            Event e = parseEvent(req);
            ctx.json(eventService.create(e, creator));
        });

        app.put("/api/eventos/{id}", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN, Role.ORGANIZADOR));
            EventRequest req = ctx.bodyAsClass(EventRequest.class);
            ctx.json(eventService.update(ctx.pathParamAsClass("id", Long.class).get(), parseEvent(req)));
        });

        app.patch("/api/eventos/{id}/status", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN, Role.ORGANIZADOR));
            String value = ctx.queryParam("value", "BORRADOR");
            ctx.json(eventService.changeStatus(ctx.pathParamAsClass("id", Long.class).get(), EventStatus.valueOf(value)));
        });

        app.delete("/api/eventos/{id}", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN));
            eventService.delete(ctx.pathParamAsClass("id", Long.class).get());
            ctx.json(Map.of("deleted", true));
        });

        app.post("/api/eventos/{id}/inscribirse", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.PARTICIPANTE, Role.ADMIN, Role.ORGANIZADOR));
            Event event = eventService.get(ctx.pathParamAsClass("id", Long.class).get());
            User user = userRepository.findById(SecurityUtil.userId(ctx)).orElseThrow();
            ctx.json(registrationService.register(event, user));
        });

        app.delete("/api/eventos/{id}/inscripcion", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.PARTICIPANTE, Role.ADMIN, Role.ORGANIZADOR));
            Event event = eventService.get(ctx.pathParamAsClass("id", Long.class).get());
            User user = userRepository.findById(SecurityUtil.userId(ctx)).orElseThrow();
            registrationService.cancel(event, user);
            ctx.json(Map.of("cancelled", true));
        });

        app.get("/api/eventos/{id}/mi-qr", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.PARTICIPANTE, Role.ADMIN, Role.ORGANIZADOR));
            Long eventId = ctx.pathParamAsClass("id", Long.class).get();
            Long userId = SecurityUtil.userId(ctx);
            ctx.json(registrationService.qrByRegistration(eventId, userId));
        });

        app.post("/api/asistencia/validar", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN, Role.ORGANIZADOR));
            TokenRequest request = ctx.bodyAsClass(TokenRequest.class);
            Registration r = registrationService.validateAttendance(request.token);
            ctx.json(Map.of("ok", true, "registrationId", r.getId(), "attendedAt", String.valueOf(r.getAttendedAt())));
        });

        app.get("/api/eventos/{id}/resumen", ctx -> ctx.json(registrationService.eventSummary(ctx.pathParamAsClass("id", Long.class).get())));
        app.get("/api/eventos/{id}/stats/inscripciones-por-dia", ctx -> ctx.json(registrationService.registrationsByDay(ctx.pathParamAsClass("id", Long.class).get())));
        app.get("/api/eventos/{id}/stats/asistencia-por-hora", ctx -> ctx.json(registrationService.attendanceByHour(ctx.pathParamAsClass("id", Long.class).get())));

        app.get("/api/admin/usuarios", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN));
            ctx.json(userService.allUsers());
        });

        app.patch("/api/admin/usuarios/{id}/rol", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN));
            RoleRequest request = ctx.bodyAsClass(RoleRequest.class);
            ctx.json(userService.changeRole(ctx.pathParamAsClass("id", Long.class).get(), Role.valueOf(request.role)));
        });

        app.patch("/api/admin/usuarios/{id}/bloqueo", ctx -> {
            SecurityUtil.requireRoles(ctx, Set.of(Role.ADMIN));
            BlockRequest request = ctx.bodyAsClass(BlockRequest.class);
            ctx.json(userService.toggleBlock(ctx.pathParamAsClass("id", Long.class).get(), request.blocked));
        });
    }

    private Event parseEvent(EventRequest req) {
        Event e = new Event();
        e.setTitle(req.title);
        e.setDescription(req.description);
        e.setDateTime(LocalDateTime.parse(req.dateTime));
        e.setLocation(req.location);
        e.setMaxCapacity(req.maxCapacity);
        return e;
    }
}

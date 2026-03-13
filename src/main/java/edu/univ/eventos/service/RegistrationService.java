package edu.univ.eventos.service;

import edu.univ.eventos.model.Event;
import edu.univ.eventos.model.Registration;
import edu.univ.eventos.model.User;
import edu.univ.eventos.repository.RegistrationRepository;
import edu.univ.eventos.util.QrUtil;

import java.time.LocalDateTime;
import java.util.*;

public class RegistrationService {
    private final RegistrationRepository registrationRepository = new RegistrationRepository();

    public Registration register(Event event, User user) {
        if (event.getDateTime().isBefore(LocalDateTime.now())) throw new AppException("El evento ya inició/finalizó");
        if (registrationRepository.findByEventAndUser(event.getId(), user.getId()).isPresent()) {
            throw new AppException("Ya estás inscrito en este evento");
        }
        long current = registrationRepository.countByEvent(event.getId());
        if (current >= event.getMaxCapacity()) throw new AppException("Cupo agotado");

        Registration r = new Registration();
        r.setEvent(event);
        r.setParticipant(user);
        String token = UUID.randomUUID().toString();
        r.setValidationToken(token);
        r.setQrPayload("eventoId:" + event.getId() + "|usuarioId:" + user.getId() + "|token:" + token);
        return registrationRepository.save(r);
    }

    public void cancel(Event event, User user) {
        if (event.getDateTime().isBefore(LocalDateTime.now())) throw new AppException("No puedes cancelar después del evento");
        Registration r = registrationRepository.findByEventAndUser(event.getId(), user.getId())
                .orElseThrow(() -> new AppException("No estabas inscrito"));
        registrationRepository.delete(r.getId());
    }

    public Map<String, Object> qrByRegistration(Long eventId, Long userId) {
        Registration r = registrationRepository.findByEventAndUser(eventId, userId)
                .orElseThrow(() -> new AppException("Inscripción no encontrada"));
        return Map.of(
                "registrationId", r.getId(),
                "payload", r.getQrPayload(),
                "qrBase64", QrUtil.generateBase64Png(r.getQrPayload()),
                "token", r.getValidationToken()
        );
    }

    public Registration validateAttendance(String token) {
        Registration r = registrationRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Token inválido"));
        if (r.isAttended()) throw new AppException("Asistencia ya registrada");
        r.setAttended(true);
        r.setAttendedAt(LocalDateTime.now());
        return registrationRepository.save(r);
    }

    public Map<String, Object> eventSummary(Long eventId) {
        long total = registrationRepository.countByEvent(eventId);
        long attended = registrationRepository.countAttendedByEvent(eventId);
        double pct = total == 0 ? 0 : ((double) attended * 100.0 / total);
        return Map.of("totalRegistrations", total, "totalAttended", attended, "attendancePct", pct);
    }

    public List<Map<String, Object>> registrationsByDay(Long eventId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : registrationRepository.registrationsByDay(eventId)) {
            out.add(Map.of("day", row[0].toString(), "count", ((Number) row[1]).longValue()));
        }
        return out;
    }

    public List<Map<String, Object>> attendanceByHour(Long eventId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : registrationRepository.attendanceByHour(eventId)) {
            out.add(Map.of("hour", ((Number) row[0]).intValue(), "count", ((Number) row[1]).longValue()));
        }
        return out;
    }
}

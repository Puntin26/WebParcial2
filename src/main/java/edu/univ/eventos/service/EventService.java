package edu.univ.eventos.service;

import edu.univ.eventos.model.*;
import edu.univ.eventos.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

public class EventService {
    private final EventRepository eventRepository = new EventRepository();

    public Event create(Event e, User creator) {
        validate(e);
        e.setCreatedBy(creator);
        return eventRepository.save(e);
    }

    public Event update(Long id, Event request) {
        Event e = eventRepository.findById(id).orElseThrow(() -> new AppException("Evento no encontrado"));
        e.setTitle(request.getTitle());
        e.setDescription(request.getDescription());
        e.setDateTime(request.getDateTime());
        e.setLocation(request.getLocation());
        e.setMaxCapacity(request.getMaxCapacity());
        validate(e);
        return eventRepository.save(e);
    }

    public Event changeStatus(Long id, EventStatus status) {
        Event e = eventRepository.findById(id).orElseThrow(() -> new AppException("Evento no encontrado"));
        e.setStatus(status);
        return eventRepository.save(e);
    }

    public List<Event> all() { return eventRepository.findAll(); }
    public List<Event> published() { return eventRepository.findPublished(); }
    public Event get(Long id) { return eventRepository.findById(id).orElseThrow(() -> new AppException("Evento no encontrado")); }
    public void delete(Long id) { eventRepository.delete(id); }

    private void validate(Event e) {
        if (e.getDateTime().isBefore(LocalDateTime.now())) throw new AppException("La fecha debe ser futura");
        if (e.getMaxCapacity() <= 0) throw new AppException("Cupo máximo inválido");
    }
}

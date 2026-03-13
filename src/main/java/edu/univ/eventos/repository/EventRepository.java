package edu.univ.eventos.repository;

import edu.univ.eventos.config.JpaConfig;
import edu.univ.eventos.model.Event;
import edu.univ.eventos.model.EventStatus;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class EventRepository {
    public Event save(Event event) {
        EntityManager em = JpaConfig.em();
        try {
            em.getTransaction().begin();
            if (event.getId() == null) em.persist(event); else event = em.merge(event);
            em.getTransaction().commit();
            return event;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Event> findById(Long id) {
        EntityManager em = JpaConfig.em();
        try { return Optional.ofNullable(em.find(Event.class, id)); }
        finally { em.close(); }
    }

    public List<Event> findAll() {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("from Event order by dateTime asc", Event.class).getResultList();
        } finally { em.close(); }
    }

    public List<Event> findPublished() {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("from Event where status = :status order by dateTime asc", Event.class)
                    .setParameter("status", EventStatus.PUBLICADO)
                    .getResultList();
        } finally { em.close(); }
    }

    public void delete(Long id) {
        EntityManager em = JpaConfig.em();
        try {
            em.getTransaction().begin();
            Event e = em.find(Event.class, id);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }
}

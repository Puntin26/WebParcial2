package edu.univ.eventos.repository;

import edu.univ.eventos.config.JpaConfig;
import edu.univ.eventos.model.Registration;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RegistrationRepository {
    public Registration save(Registration registration) {
        EntityManager em = JpaConfig.em();
        try {
            em.getTransaction().begin();
            if (registration.getId() == null) em.persist(registration); else registration = em.merge(registration);
            em.getTransaction().commit();
            return registration;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public Optional<Registration> findByEventAndUser(Long eventId, Long userId) {
        EntityManager em = JpaConfig.em();
        try {
            List<Registration> list = em.createQuery("from Registration r where r.event.id=:eventId and r.participant.id=:userId", Registration.class)
                    .setParameter("eventId", eventId)
                    .setParameter("userId", userId)
                    .getResultList();
            return list.stream().findFirst();
        } finally { em.close(); }
    }

    public long countByEvent(Long eventId) {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("select count(r) from Registration r where r.event.id=:eventId", Long.class)
                    .setParameter("eventId", eventId)
                    .getSingleResult();
        } finally { em.close(); }
    }

    public long countAttendedByEvent(Long eventId) {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("select count(r) from Registration r where r.event.id=:eventId and r.attended=true", Long.class)
                    .setParameter("eventId", eventId)
                    .getSingleResult();
        } finally { em.close(); }
    }

    public Optional<Registration> findByToken(String token) {
        EntityManager em = JpaConfig.em();
        try {
            List<Registration> list = em.createQuery("from Registration r where r.validationToken=:token", Registration.class)
                    .setParameter("token", token)
                    .getResultList();
            return list.stream().findFirst();
        } finally { em.close(); }
    }

    public void delete(Long registrationId) {
        EntityManager em = JpaConfig.em();
        try {
            em.getTransaction().begin();
            Registration r = em.find(Registration.class, registrationId);
            if (r != null) em.remove(r);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public List<Object[]> registrationsByDay(Long eventId) {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("select cast(r.registeredAt as date), count(r) from Registration r where r.event.id=:eventId group by cast(r.registeredAt as date) order by cast(r.registeredAt as date)", Object[].class)
                    .setParameter("eventId", eventId)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Object[]> attendanceByHour(Long eventId) {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("select hour(r.attendedAt), count(r) from Registration r where r.event.id=:eventId and r.attended=true and r.attendedAt is not null group by hour(r.attendedAt) order by hour(r.attendedAt)", Object[].class)
                    .setParameter("eventId", eventId)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Registration> findByEvent(Long eventId) {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("from Registration r where r.event.id=:eventId", Registration.class)
                    .setParameter("eventId", eventId)
                    .getResultList();
        } finally { em.close(); }
    }
}

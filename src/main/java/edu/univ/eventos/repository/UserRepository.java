package edu.univ.eventos.repository;

import edu.univ.eventos.config.JpaConfig;
import edu.univ.eventos.model.User;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    public Optional<User> findByEmail(String email) {
        EntityManager em = JpaConfig.em();
        try {
            List<User> users = em.createQuery("from User where email = :email", User.class)
                    .setParameter("email", email)
                    .getResultList();
            return users.stream().findFirst();
        } finally {
            em.close();
        }
    }

    public Optional<User> findById(Long id) {
        EntityManager em = JpaConfig.em();
        try {
            return Optional.ofNullable(em.find(User.class, id));
        } finally {
            em.close();
        }
    }

    public List<User> findAll() {
        EntityManager em = JpaConfig.em();
        try {
            return em.createQuery("from User order by createdAt desc", User.class).getResultList();
        } finally {
            em.close();
        }
    }

    public User save(User user) {
        EntityManager em = JpaConfig.em();
        try {
            em.getTransaction().begin();
            User managed = user.getId() == null ? user : em.merge(user);
            if (user.getId() == null) em.persist(user);
            em.getTransaction().commit();
            return managed;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}

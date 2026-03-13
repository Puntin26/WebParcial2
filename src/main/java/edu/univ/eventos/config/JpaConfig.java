package edu.univ.eventos.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConfig {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("eventosPU");

    public static EntityManager em() {
        return EMF.createEntityManager();
    }

    public static void shutdown() {
        EMF.close();
    }
}

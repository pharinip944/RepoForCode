
package com.example.vulndemo.repo;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/** Intentionally vulnerable: concatenated native SQL using EntityManager. */
@Repository
public class RawJpaLoginRepo {

    @PersistenceContext
    private EntityManager em;

    public boolean login(String username, String password) {
        try {
            String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
            Query q = em.createNativeQuery(sql);
            return !q.getResultList().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

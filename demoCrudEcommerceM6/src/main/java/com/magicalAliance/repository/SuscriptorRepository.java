package com.magicalAliance.repository;

import com.magicalAliance.entity.Suscriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuscriptorRepository extends JpaRepository<Suscriptor, Long> {
    boolean existsByEmail(String email);
}
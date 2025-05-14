package com.felicita.repositorios;

import com.felicita.entidades.Barberia;
import com.felicita.entidades.ProAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberiaRepositorio extends JpaRepository<Barberia, Long> {
    List<Barberia> findByProAdmin(ProAdmin proAdmin);
    
    @Query("SELECT b FROM Barberia b WHERE b.proAdmin.id = :proAdminId AND b.estaActivo = true")
    List<Barberia> findActiveByProAdminId(@Param("proAdminId") Long proAdminId);
    
    @Query("SELECT b FROM Barberia b WHERE b.tieneServiciosBarba = true")
    List<Barberia> findByTieneServiciosBarba();
    
    @Query("SELECT b FROM Barberia b WHERE LOWER(b.especialidadCortes) LIKE LOWER(CONCAT('%', :especialidad, '%'))")
    List<Barberia> findByEspecialidadCortesContainingIgnoreCase(@Param("especialidad") String especialidad);

    
}
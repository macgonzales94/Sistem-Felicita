package com.felicita.repositorios;

import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.SalonBelleza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalonBellezaRepositorio extends JpaRepository<SalonBelleza, Long> {
    List<SalonBelleza> findByProAdmin(ProAdmin proAdmin);
    
    @Query("SELECT s FROM SalonBelleza s WHERE s.proAdmin.id = :proAdminId AND s.estaActivo = true")
    List<SalonBelleza> findActiveByProAdminId(@Param("proAdminId") Long proAdminId);
    
    @Query("SELECT s FROM SalonBelleza s WHERE s.tieneServiciosMaquillaje = true")
    List<SalonBelleza> findByTieneServiciosMaquillaje();
    
    @Query("SELECT s FROM SalonBelleza s WHERE s.tieneServiciosUnas = true")
    List<SalonBelleza> findByTieneServiciosUnas();
    
    @Query("SELECT s FROM SalonBelleza s WHERE s.tieneTratamientosCapilares = true")
    List<SalonBelleza> findByTieneTratamientosCapilares();
    
    @Query("SELECT s FROM SalonBelleza s WHERE LOWER(s.especialidad) LIKE LOWER(CONCAT('%', :especialidad, '%'))")
    List<SalonBelleza> findByEspecialidadContainingIgnoreCase(@Param("especialidad") String especialidad);
}
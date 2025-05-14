package com.felicita.repositorios;

import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServicioRepositorio extends JpaRepository<Servicio, Long> {
    List<Servicio> findByEstablecimiento(Establecimiento establecimiento);
    
    Page<Servicio> findByEstablecimiento(Establecimiento establecimiento, Pageable pageable);
    
    @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.estaDisponible = true")
    List<Servicio> findDisponiblesByEstablecimientoId(@Param("establecimientoId") Long establecimientoId);
    
    @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.categoria = :categoria")
    List<Servicio> findByEstablecimientoIdAndCategoria(
            @Param("establecimientoId") Long establecimientoId, 
            @Param("categoria") String categoria);
    
    @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.precio BETWEEN :precioMin AND :precioMax")
    List<Servicio> findByEstablecimientoIdAndPrecioBetween(
            @Param("establecimientoId") Long establecimientoId, 
            @Param("precioMin") BigDecimal precioMin, 
            @Param("precioMax") BigDecimal precioMax);
}
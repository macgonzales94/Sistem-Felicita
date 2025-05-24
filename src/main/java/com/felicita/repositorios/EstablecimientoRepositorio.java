package com.felicita.repositorios;

import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.ProAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstablecimientoRepositorio extends JpaRepository<Establecimiento, Long> {
    List<Establecimiento> findByProAdmin(ProAdmin proAdmin);

    Page<Establecimiento> findByProAdmin(ProAdmin proAdmin, Pageable pageable);

    @Query("SELECT e FROM Establecimiento e WHERE e.proAdmin.id = :proAdminId AND e.estaActivo = true")
    List<Establecimiento> findActiveByProAdminId(@Param("proAdminId") Long proAdminId);

    @Query("SELECT e FROM Establecimiento e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Establecimiento> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT e FROM Establecimiento e WHERE LOWER(e.ciudad) = LOWER(:ciudad)")
    List<Establecimiento> findByCiudadIgnoreCase(@Param("ciudad") String ciudad);

    @Query("SELECT COUNT(e) > 0 FROM Establecimiento e WHERE e.proAdmin.id = :proAdminId")
    boolean existsByProAdminId(@Param("proAdminId") Long proAdminId);

    // Métodos adicionales para EstablecimientoRepositorio.java

    @Query("SELECT COUNT(e) FROM Establecimiento e WHERE e.proAdmin.id = :proAdminId")
    long countByProAdminId(@Param("proAdminId") Long proAdminId);

    @Query("SELECT e.id FROM Establecimiento e WHERE e.proAdmin.id = :proAdminId")
    List<Long> findIdsByProAdminId(@Param("proAdminId") Long proAdminId);

    /**
     * Buscar establecimientos activos
     */
    @Query("SELECT e FROM Establecimiento e WHERE e.estaActivo = true ORDER BY e.nombre")
    List<Establecimiento> findEstablecimientosActivos();

    /**
     * Buscar establecimientos por filtros múltiples
     */
    @Query("SELECT e FROM Establecimiento e WHERE e.estaActivo = true " +
            "AND (:ciudad IS NULL OR LOWER(e.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%'))) " +
            "AND (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Establecimiento> findByFiltros(
            @Param("ciudad") String ciudad,
            @Param("nombre") String nombre);

    /**
     * Contar establecimientos activos por ciudad
     */
    @Query("SELECT COUNT(e) FROM Establecimiento e WHERE e.estaActivo = true AND LOWER(e.ciudad) = LOWER(:ciudad)")
    long countEstablecimientosActivosByCiudad(@Param("ciudad") String ciudad);

}
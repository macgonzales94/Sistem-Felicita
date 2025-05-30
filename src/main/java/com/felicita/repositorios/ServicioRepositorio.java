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
import java.util.Optional;

@Repository
public interface ServicioRepositorio extends JpaRepository<Servicio, Long> {

       /**
        * Buscar servicios por establecimiento ordenados por categoría y nombre
        */
       List<Servicio> findByEstablecimientoIdOrderByCategoriaAscNombreAsc(Long establecimientoId);

       /**
        * Buscar servicios disponibles por establecimiento
        */
       List<Servicio> findByEstablecimientoIdAndEstaDisponibleTrueOrderByCategoriaAscNombreAsc(Long establecimientoId);

       /**
        * Buscar servicios por establecimiento con paginación
        */
       Page<Servicio> findByEstablecimientoId(Long establecimientoId, Pageable pageable);

       /**
        * Verificar si existe un servicio con el mismo nombre en el establecimiento
        */
       boolean existsByNombreAndEstablecimientoId(String nombre, Long establecimientoId);

       /**
        * Verificar si existe otro servicio con el mismo nombre (excluyendo uno
        * específico)
        */
       @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
                     "FROM Servicio s WHERE s.nombre = :nombre AND s.establecimiento.id = :establecimientoId AND s.id != :id")
       boolean existsByNombreAndEstablecimientoIdAndIdNot(@Param("nombre") String nombre,
                     @Param("establecimientoId") Long establecimientoId,
                     @Param("id") Long id);

       /**
        * Buscar servicios por categoría en un establecimiento
        */
       List<Servicio> findByEstablecimientoIdAndCategoriaOrderByNombreAsc(Long establecimientoId, String categoria);

       /**
        * Buscar servicios por nombre (búsqueda parcial) en un establecimiento
        */
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId " +
                     "AND LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
                     "ORDER BY s.categoria ASC, s.nombre ASC")
       List<Servicio> findByEstablecimientoIdAndNombreContainingIgnoreCase(
                     @Param("establecimientoId") Long establecimientoId,
                     @Param("nombre") String nombre);

       /**
        * Buscar servicios activos por establecimiento
        */
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId " +
                     "AND s.estaDisponible = true AND s.establecimiento.estaActivo = true " +
                     "ORDER BY s.categoria ASC, s.nombre ASC")
       List<Servicio> findServiciosActivosPorEstablecimiento(@Param("establecimientoId") Long establecimientoId);

       /**
        * Contar servicios por establecimiento
        */
       long countByEstablecimientoId(Long establecimientoId);

       /**
        * Contar servicios disponibles por establecimiento
        */
       long countByEstablecimientoIdAndEstaDisponibleTrue(Long establecimientoId);

       /**
        * Obtener categorías únicas de servicios por establecimiento
        */
       @Query("SELECT DISTINCT s.categoria FROM Servicio s WHERE s.establecimiento.id = :establecimientoId ORDER BY s.categoria")
       List<String> findDistinctCategoriasByEstablecimientoId(@Param("establecimientoId") Long establecimientoId);

       /**
        * Buscar servicios por rango de precios en un establecimiento
        */
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId " +
                     "AND s.precio BETWEEN :precioMin AND :precioMax " +
                     "ORDER BY s.precio ASC")
       List<Servicio> findByEstablecimientoIdAndPrecioBetween(@Param("establecimientoId") Long establecimientoId,
                     @Param("precioMin") java.math.BigDecimal precioMin,
                     @Param("precioMax") java.math.BigDecimal precioMax);

       /**
        * Buscar servicios por duración en un establecimiento
        */
       List<Servicio> findByEstablecimientoIdAndDuracionMinutosOrderByNombreAsc(Long establecimientoId,
                     Integer duracionMinutos);

       /**
        * Obtener servicios más populares por establecimiento (basado en número de
        * citas)
        */
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId " +
                     "ORDER BY SIZE(s.citas) DESC")
       List<Servicio> findServiciosPopularesPorEstablecimiento(@Param("establecimientoId") Long establecimientoId);

       /**
        * Buscar servicios que pertenecen a un ProAdmin específico
        */
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.proAdmin.id = :proAdminId " +
                     "ORDER BY s.establecimiento.nombre ASC, s.categoria ASC, s.nombre ASC")
       List<Servicio> findByProAdminId(@Param("proAdminId") Long proAdminId);

       /**
        * Verificar si un servicio pertenece a un ProAdmin específico
        */
       @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
                     "FROM Servicio s WHERE s.id = :servicioId AND s.establecimiento.proAdmin.id = :proAdminId")
       boolean existsByIdAndProAdminId(@Param("servicioId") Long servicioId, @Param("proAdminId") Long proAdminId);

       /**
        * Buscar servicios por filtros múltiples
        */
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId " +
                     "AND (:categoria IS NULL OR s.categoria = :categoria) " +
                     "AND (:disponible IS NULL OR s.estaDisponible = :disponible) " +
                     "AND (:nombre IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
                     "ORDER BY s.categoria ASC, s.nombre ASC")
       List<Servicio> findByFiltrosMultiples(@Param("establecimientoId") Long establecimientoId,
                     @Param("categoria") String categoria,
                     @Param("disponible") Boolean disponible,
                     @Param("nombre") String nombre);

       /**
        * Obtener estadísticas de servicios por establecimiento
        */
       @Query("SELECT COUNT(s), AVG(s.precio), MIN(s.precio), MAX(s.precio) " +
                     "FROM Servicio s WHERE s.establecimiento.id = :establecimientoId")
       Object[] getEstadisticasServiciosPorEstablecimiento(@Param("establecimientoId") Long establecimientoId);

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

       // MÉTODO AGREGADO - Este era el que faltaba
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.estaDisponible = true ORDER BY s.nombre")
       List<Servicio> findServiciosDisponiblesByEstablecimientoId(@Param("establecimientoId") Long establecimientoId);

       // Métodos adicionales útiles
       @Query("SELECT s FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.estaDisponible = true AND s.categoria = :categoria")
       List<Servicio> findDisponiblesByEstablecimientoIdAndCategoria(
                     @Param("establecimientoId") Long establecimientoId,
                     @Param("categoria") String categoria);

       @Query("SELECT DISTINCT s.categoria FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.estaDisponible = true AND s.categoria IS NOT NULL")
       List<String> findCategoriasByEstablecimientoId(@Param("establecimientoId") Long establecimientoId);

       @Query("SELECT COUNT(s) FROM Servicio s WHERE s.establecimiento.id = :establecimientoId AND s.estaDisponible = true")
       long countServiciosDisponiblesByEstablecimientoId(@Param("establecimientoId") Long establecimientoId);

}
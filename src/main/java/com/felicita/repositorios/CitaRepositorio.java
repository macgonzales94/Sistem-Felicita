package com.felicita.repositorios;

import com.felicita.entidades.Cita;
import com.felicita.entidades.Cliente;
import com.felicita.entidades.Establecimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepositorio extends JpaRepository<Cita, Long> {
    
    // Métodos existentes
    List<Cita> findByCliente(Cliente cliente);
    List<Cita> findByEstablecimiento(Establecimiento establecimiento);
    Optional<Cita> findByCodigoUnico(String codigoUnico);

    // Métodos necesarios para CitaClienteServicioImpl
    Page<Cita> findByClienteOrderByFechaHoraDesc(Cliente cliente, Pageable pageable);
    
    List<Cita> findByClienteAndEstadoOrderByFechaHoraDesc(Cliente cliente, Cita.EstadoCita estado);

    @Query("SELECT c FROM Cita c WHERE c.establecimiento.id = :establecimientoId AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<Cita> findByEstablecimientoAndRangoFecha(
            @Param("establecimientoId") Long establecimientoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.cliente.id = :clienteId AND c.fechaHora > :ahora")
    long countCitasFuturasByCliente(@Param("clienteId") Long clienteId, @Param("ahora") LocalDateTime ahora);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.establecimiento.id IN :idsEstablecimientos")
    long countByEstablecimientoIdIn(@Param("idsEstablecimientos") List<Long> idsEstablecimientos);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.establecimiento.id IN :idsEstablecimientos AND c.estado = :estado")
    long countByEstablecimientoIdInAndEstado(
            @Param("idsEstablecimientos") List<Long> idsEstablecimientos,
            @Param("estado") Cita.EstadoCita estado);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.establecimiento.id IN :idsEstablecimientos AND c.fechaHora BETWEEN :inicio AND :fin")
    long countByEstablecimientoIdInAndFechaHoraBetween(
            @Param("idsEstablecimientos") List<Long> idsEstablecimientos,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
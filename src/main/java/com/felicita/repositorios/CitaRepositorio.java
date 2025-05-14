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
    List<Cita> findByCliente(Cliente cliente);
    
    List<Cita> findByEstablecimiento(Establecimiento establecimiento);
    
    Page<Cita> findByClienteOrderByFechaHoraDesc(Cliente cliente, Pageable pageable);
    
    List<Cita> findByClienteAndEstadoOrderByFechaHoraDesc(Cliente cliente, Cita.EstadoCita estado);
    
    @Query("SELECT c FROM Cita c WHERE c.establecimiento.id = :establecimientoId AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<Cita> findByEstablecimientoAndRangoFecha(
            @Param("establecimientoId") Long establecimientoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
    
    Optional<Cita> findByCodigoUnico(String codigoUnico);
    
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.cliente.id = :clienteId AND c.fechaHora > :ahora")
    long countCitasFuturasByCliente(@Param("clienteId") Long clienteId, @Param("ahora") LocalDateTime ahora);
}
package com.felicita.repositorios;

import com.felicita.entidades.Cliente;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GiftCardRepositorio extends JpaRepository<GiftCard, Long> {
    Optional<GiftCard> findByCodigo(String codigo);
    
    List<GiftCard> findByCliente(Cliente cliente);
    
    List<GiftCard> findByEstablecimiento(Establecimiento establecimiento);
    
    @Query("SELECT g FROM GiftCard g WHERE g.cliente.id = :clienteId AND g.activa = true AND g.fechaExpiracion >= :hoy ORDER BY g.fechaExpiracion ASC")
    List<GiftCard> findGiftCardsActivasByCliente(
            @Param("clienteId") Long clienteId, 
            @Param("hoy") LocalDate hoy);
    
    @Query("SELECT g FROM GiftCard g WHERE g.establecimiento.id = :establecimientoId AND g.activa = true")
    List<GiftCard> findGiftCardsActivasByEstablecimiento(@Param("establecimientoId") Long establecimientoId);
}
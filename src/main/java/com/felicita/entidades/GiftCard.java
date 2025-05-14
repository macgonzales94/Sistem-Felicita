package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", unique = true, nullable = false, length = 16)
    private String codigo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;
    
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Column(name = "saldo", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo;
    
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;
    
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDate fechaExpiracion;
    
    @Column(name = "mensaje", length = 200)
    private String mensaje;
    
    @Column(name = "activa", nullable = false)
    private boolean activa = true;
    
    @PrePersist
    protected void onCreate() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
        if (fechaExpiracion == null) {
            // Por defecto, las gift cards expiran en 1 año
            fechaExpiracion = fechaEmision.toLocalDate().plusYears(1);
        }
        if (saldo == null) {
            saldo = valor;
        }
        if (codigo == null) {
            codigo = generarCodigo();
        }
    }
    
    private String generarCodigo() {
        // Generar un código único alfanumérico de 16 caracteres
        String base = "GC" + System.currentTimeMillis() % 10000000000L;
        // Asegurar que tenga exactamente 16 caracteres
        if (base.length() < 16) {
            StringBuilder sb = new StringBuilder(base);
            while (sb.length() < 16) {
                sb.append('0');
            }
            return sb.toString();
        } else {
            return base.substring(0, 16);
        }
    }
    
    public boolean esValida() {
        return activa && 
               saldo.compareTo(BigDecimal.ZERO) > 0 && 
               !fechaExpiracion.isBefore(LocalDate.now());
    }
}
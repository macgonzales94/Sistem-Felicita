package com.felicita.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftCardDTO {
    private Long id;
    private String codigo;
    
    private Long clienteId;
    
    @NotNull(message = "El establecimiento es obligatorio")
    private Long establecimientoId;
    private String nombreEstablecimiento;
    
    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "10.0", message = "El valor mínimo es 10.00")
    private BigDecimal valor;
    
    private BigDecimal saldo;
    
    private LocalDateTime fechaEmision;
    
    private LocalDate fechaExpiracion;
    
    @Size(max = 200, message = "El mensaje no puede exceder los 200 caracteres")
    private String mensaje;
    
    private boolean activa;
    private boolean valida;
    
    // Datos para envío de regalo (opcional)
    private String emailDestinatario;
    private String nombreDestinatario;
    private String mensajePersonalizado;
}
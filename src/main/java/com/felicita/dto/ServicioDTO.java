package com.felicita.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    private BigDecimal precio;
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 5, message = "La duración mínima debe ser de 5 minutos")
    private Integer duracionMinutos;
    
    @Size(max = 50, message = "La categoría no puede exceder los 50 caracteres")
    private String categoria;
    
    private String imagenUrl;
    
    private boolean estaDisponible;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    @NotNull(message = "El establecimiento es obligatorio")
    private Long establecimientoId;
    private String nombreEstablecimiento;
}
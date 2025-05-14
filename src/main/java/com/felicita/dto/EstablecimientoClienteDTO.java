package com.felicita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablecimientoClienteDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;  // "BARBERIA" o "SALON_BELLEZA"
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String telefono;
    private String email;
    private String sitioWeb;
    private String horariosAtencion;
    private String imagenUrl;
    private Double calificacionPromedio;
    private Integer cantidadResenas;
    private List<String> caracteristicas = new ArrayList<>();
    private List<ServicioClienteDTO> serviciosDestacados = new ArrayList<>();
    private boolean abierto;
    
    // Detalles específicos para barberías o salones
    private boolean tieneBarba; // Para barberías
    private boolean tieneMaquillaje; // Para salones
    private String especialidad;
}
package com.felicita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    private long totalEstablecimientos;
    private long totalCitas;
    private long citasPendientes;
    private long citasConfirmadas;
    private long citasCompletadas;
    private long citasCanceladas;
    private long citasNoAsistio;
    
    private long citasHoy;
    private long citasSemana;
    private long citasMes;
    
    private double tasaAsistencia;
    private double tasaCancelacion;
    
    private Map<String, Long> citasPorServicio = new HashMap<>();
    private Map<String, Long> citasPorDiaSemana = new HashMap<>();
    private Map<String, Long> citasPorHora = new HashMap<>();
    private Map<String, Long> citasPorMes = new HashMap<>();
    
    // Ingresos
    private double ingresosTotales;
    private double ingresosMes;
    private double ingresosSemana;
    
    // Datos complementarios
    private Map<String, Object> datosAdicionales = new HashMap<>();
    
    // Método de utilidad para agregar datos adicionales
    public void agregarDatoAdicional(String clave, Object valor) {
        this.datosAdicionales.put(clave, valor);
    }
}
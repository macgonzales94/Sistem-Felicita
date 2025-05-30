package com.felicita.controladores;

import com.felicita.dto.EstablecimientoClienteDTO;
import com.felicita.dto.ServicioClienteDTO;
import com.felicita.servicios.EstablecimientoClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para APIs públicas de servicios que se muestran en la página principal
 */
@RestController
@RequestMapping("/api/publico")
public class ApiServiciosControlador {

    @Autowired
    private EstablecimientoClienteServicio establecimientoClienteServicio;

    /**
     * Obtener servicios populares para la página principal
     */
    @GetMapping("/servicios-populares")
    public ResponseEntity<Map<String, List<ServicioClienteDTO>>> obtenerServiciosPopulares(
            @RequestParam(defaultValue = "4") int limitePorCategoria) {
        
        try {
            Map<String, List<ServicioClienteDTO>> serviciosPorCategoria = new HashMap<>();
            
            // Obtener establecimientos destacados
            List<EstablecimientoClienteDTO> establecimientos = 
                establecimientoClienteServicio.obtenerMejorCalificados(10);
            
            // Recopilar servicios de estos establecimientos
            for (EstablecimientoClienteDTO est : establecimientos) {
                try {
                    List<ServicioClienteDTO> serviciosEst = 
                        establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(est.getId());
                    
                    // Agrupar por categoría
                    Map<String, List<ServicioClienteDTO>> agrupados = serviciosEst.stream()
                        .filter(servicio -> servicio.getCategoria() != null && 
                                          !servicio.getCategoria().isEmpty() &&
                                          servicio.isDisponible())
                        .collect(Collectors.groupingBy(ServicioClienteDTO::getCategoria));
                    
                    // Agregar a la colección principal limitando por categoría
                    agrupados.forEach((categoria, servicios) -> {
                        serviciosPorCategoria.computeIfAbsent(categoria, k -> new java.util.ArrayList<>())
                            .addAll(servicios.stream().limit(limitePorCategoria).collect(Collectors.toList()));
                    });
                    
                } catch (Exception e) {
                    // Continuar con el siguiente establecimiento si hay error
                    continue;
                }
            }
            
            // Limitar finalmente por categoría
            serviciosPorCategoria.replaceAll((categoria, servicios) -> 
                servicios.stream().limit(limitePorCategoria).collect(Collectors.toList()));
            
            return ResponseEntity.ok(serviciosPorCategoria);
            
        } catch (Exception e) {
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    /**
     * Obtener establecimientos destacados para la página principal
     */
    @GetMapping("/establecimientos-destacados")
    public ResponseEntity<List<EstablecimientoClienteDTO>> obtenerEstablecimientosDestacados(
            @RequestParam(defaultValue = "6") int limite) {
        
        try {
            List<EstablecimientoClienteDTO> establecimientos = 
                establecimientoClienteServicio.obtenerMejorCalificados(limite);
            return ResponseEntity.ok(establecimientos);
        } catch (Exception e) {
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    /**
     * Búsqueda rápida para autocompletado
     */
    @GetMapping("/busqueda-rapida")
    public ResponseEntity<Map<String, Object>> busquedaRapida(@RequestParam String termino) {
        
        Map<String, Object> resultados = new HashMap<>();
        
        try {
            // Buscar establecimientos por nombre
            List<EstablecimientoClienteDTO> establecimientos = 
                establecimientoClienteServicio.buscarPorNombre(termino)
                .stream().limit(5).collect(Collectors.toList());
            
            resultados.put("establecimientos", establecimientos);
            
            // Buscar servicios (simulado - en una implementación real tendrías un servicio específico)
            List<String> serviciosSugeridos = obtenerServiciosSugeridos(termino);
            resultados.put("servicios", serviciosSugeridos);
            
            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    /**
     * Obtener estadísticas para la página principal
     */
    @GetMapping("/estadisticas-generales")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGenerales() {
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Obtener establecimientos para contar
            List<EstablecimientoClienteDTO> establecimientos = 
                establecimientoClienteServicio.obtenerMejorCalificados(100); // Obtener muchos para contar
            
            // Contar servicios
            int totalServicios = 0;
            for (EstablecimientoClienteDTO est : establecimientos) {
                try {
                    List<ServicioClienteDTO> servicios = 
                        establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(est.getId());
                    totalServicios += servicios.size();
                } catch (Exception e) {
                    // Continuar contando
                }
            }
            
            estadisticas.put("totalEstablecimientos", establecimientos.size());
            estadisticas.put("totalServicios", totalServicios);
            estadisticas.put("calificacionPromedio", 4.8);
            estadisticas.put("totalReservas", 1250); // Valor simulado
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            // Retornar estadísticas por defecto
            estadisticas.put("totalEstablecimientos", 0);
            estadisticas.put("totalServicios", 0);
            estadisticas.put("calificacionPromedio", 0.0);
            estadisticas.put("totalReservas", 0);
            
            return ResponseEntity.ok(estadisticas);
        }
    }

    /**
     * Obtener ciudades disponibles
     */
    @GetMapping("/ciudades")
    public ResponseEntity<List<String>> obtenerCiudades() {
        
        try {
            List<EstablecimientoClienteDTO> establecimientos = 
                establecimientoClienteServicio.obtenerMejorCalificados(100);
            
            List<String> ciudades = establecimientos.stream()
                .map(EstablecimientoClienteDTO::getCiudad)
                .filter(ciudad -> ciudad != null && !ciudad.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ciudades);
            
        } catch (Exception e) {
            // Retornar ciudades por defecto
            List<String> ciudadesDefault = java.util.Arrays.asList(
                "Lima", "Miraflores", "San Isidro", "Surco", "La Molina"
            );
            return ResponseEntity.ok(ciudadesDefault);
        }
    }

    /**
     * Verificar estado del sistema
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> verificarEstado() {
        
        Map<String, Object> estado = new HashMap<>();
        
        try {
            // Verificar que los servicios estén funcionando
            List<EstablecimientoClienteDTO> test = 
                establecimientoClienteServicio.obtenerMejorCalificados(1);
            
            estado.put("sistemaActivo", true);
            estado.put("serviciosDisponibles", true);
            estado.put("timestamp", java.time.LocalDateTime.now());
            estado.put("version", "1.0.0");
            
        } catch (Exception e) {
            estado.put("sistemaActivo", false);
            estado.put("serviciosDisponibles", false);
            estado.put("error", e.getMessage());
            estado.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return ResponseEntity.ok(estado);
    }

    /**
     * Método privado para obtener servicios sugeridos
     */
    private List<String> obtenerServiciosSugeridos(String termino) {
        List<String> todosLosServicios = java.util.Arrays.asList(
            "Corte de cabello", "Corte y barba", "Arreglo de barba", "Afeitado clásico",
            "Maquillaje", "Manicura", "Pedicura", "Tratamiento capilar", "Coloración",
            "Peinado", "Depilación", "Tratamiento facial", "Spa", "Masaje", "Tinte",
            "Mechas", "Alisado", "Rizado", "Extensiones", "Cejas", "Pestañas"
        );
        
        return todosLosServicios.stream()
            .filter(servicio -> servicio.toLowerCase().contains(termino.toLowerCase()))
            .limit(8)
            .collect(Collectors.toList());
    }
}
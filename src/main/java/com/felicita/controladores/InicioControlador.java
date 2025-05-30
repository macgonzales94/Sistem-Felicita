package com.felicita.controladores;

import com.felicita.dto.EstablecimientoClienteDTO;
import com.felicita.dto.ServicioClienteDTO;
import com.felicita.servicios.EstablecimientoClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class InicioControlador {

    @Autowired
    private EstablecimientoClienteServicio establecimientoClienteServicio;

    @GetMapping("/")
    public String inicio(Model model) {
        try {
            // Verificar autenticación
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAutenticado = auth != null && auth.isAuthenticated() && 
                                  !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"));
            
            model.addAttribute("isAutenticado", isAutenticado);
            
            if (isAutenticado) {
                model.addAttribute("nombreUsuario", auth.getName());
                model.addAttribute("roles", auth.getAuthorities());
            }
            
            // Cargar datos principales
            cargarDatosParaPaginaPrincipal(model);
            
        } catch (Exception e) {
            System.err.println("Error en InicioControlador: " + e.getMessage());
            e.printStackTrace();
            // Cargar datos por defecto en caso de error
            cargarDatosPorDefecto(model);
        }
        
        return "index";
    }
    
    @GetMapping("/acerca")
    public String acerca() {
        return "acerca";
    }
    
    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }
    
    /**
     * Cargar datos para la página principal de forma segura
     */
    private void cargarDatosParaPaginaPrincipal(Model model) {
        // 1. Cargar establecimientos destacados
        List<EstablecimientoClienteDTO> establecimientosDestacados = cargarEstablecimientosSeguro();
        model.addAttribute("establecimientosDestacados", establecimientosDestacados);
        
        // 2. Cargar servicios populares por categoría
        Map<String, List<ServicioClienteDTO>> serviciosPorCategoria = cargarServiciosPopularesSeguro(establecimientosDestacados);
        model.addAttribute("serviciosPorCategoria", serviciosPorCategoria);
        
        // 3. Calcular estadísticas generales
        Map<String, Object> estadisticasGenerales = calcularEstadisticas(establecimientosDestacados, serviciosPorCategoria);
        model.addAttribute("estadisticasGenerales", estadisticasGenerales);
    }
    
    /**
     * Cargar establecimientos de forma segura
     */
    private List<EstablecimientoClienteDTO> cargarEstablecimientosSeguro() {
        try {
            if (establecimientoClienteServicio == null) {
                System.err.println("ADVERTENCIA: establecimientoClienteServicio es null");
                return new ArrayList<>();
            }
            
            List<EstablecimientoClienteDTO> establecimientos = establecimientoClienteServicio.obtenerMejorCalificados(6);
            
            if (establecimientos == null) {
                System.err.println("ADVERTENCIA: obtenerMejorCalificados retornó null");
                return new ArrayList<>();
            }
            
            // Validar que los establecimientos tengan datos básicos
            List<EstablecimientoClienteDTO> establecimientosValidos = establecimientos.stream()
                .filter(est -> est != null && est.getNombre() != null && !est.getNombre().trim().isEmpty())
                .collect(Collectors.toList());
            
            return establecimientosValidos;
            
        } catch (Exception e) {
            System.err.println("Error cargando establecimientos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Cargar servicios populares de forma segura
     */
    private Map<String, List<ServicioClienteDTO>> cargarServiciosPopularesSeguro(List<EstablecimientoClienteDTO> establecimientos) {
        Map<String, List<ServicioClienteDTO>> serviciosPorCategoria = new HashMap<>();
        
        try {
            if (establecimientos == null || establecimientos.isEmpty()) {
                return serviciosPorCategoria;
            }
            
            // Obtener servicios de cada establecimiento
            for (EstablecimientoClienteDTO establecimiento : establecimientos) {
                try {
                    if (establecimiento.getId() != null) {
                        List<ServicioClienteDTO> serviciosEst = establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(establecimiento.getId());
                        
                        if (serviciosEst != null && !serviciosEst.isEmpty()) {
                            // Procesar servicios válidos
                            for (ServicioClienteDTO servicio : serviciosEst) {
                                if (esServicioValido(servicio)) {
                                    // Normalizar nombre de categoría
                                    String categoriaNormalizada = normalizarCategoria(servicio.getCategoria());
                                    
                                    serviciosPorCategoria.computeIfAbsent(categoriaNormalizada, k -> new ArrayList<>())
                                        .add(completarDatosServicio(servicio, establecimiento));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando servicios del establecimiento " + establecimiento.getId() + ": " + e.getMessage());
                    // Continuar con el siguiente establecimiento
                }
            }
            
            // Limitar servicios por categoría y ordenar por popularidad
            serviciosPorCategoria.replaceAll((categoria, servicios) -> 
                servicios.stream()
                    .limit(4)
                    .collect(Collectors.toList())
            );
            
        } catch (Exception e) {
            System.err.println("Error general cargando servicios: " + e.getMessage());
        }
        
        return serviciosPorCategoria;
    }
    
    /**
     * Validar que un servicio tenga datos mínimos requeridos
     */
    private boolean esServicioValido(ServicioClienteDTO servicio) {
        return servicio != null &&
               servicio.getNombre() != null && !servicio.getNombre().trim().isEmpty() &&
               servicio.getCategoria() != null && !servicio.getCategoria().trim().isEmpty() &&
               servicio.getPrecio() != null &&
               servicio.isDisponible();
    }
    
    /**
     * Normalizar nombres de categorías para consistencia
     */
    private String normalizarCategoria(String categoria) {
        if (categoria == null) return "Otros";
        
        String categoriaNormalizada = categoria.trim();
        
        // Mapear categorías similares
        if (categoriaNormalizada.toLowerCase().contains("corte")) {
            return "Cortes";
        } else if (categoriaNormalizada.toLowerCase().contains("barba")) {
            return "Barba";
        } else if (categoriaNormalizada.toLowerCase().contains("maquillaje")) {
            return "Maquillaje";
        } else if (categoriaNormalizada.toLowerCase().contains("manicura") || categoriaNormalizada.toLowerCase().contains("uña")) {
            return "Manicura";
        } else if (categoriaNormalizada.toLowerCase().contains("tratamiento") || categoriaNormalizada.toLowerCase().contains("spa")) {
            return "Tratamientos";
        }
        
        return categoriaNormalizada;
    }
    
    /**
     * Completar datos del servicio con información del establecimiento
     */
    private ServicioClienteDTO completarDatosServicio(ServicioClienteDTO servicio, EstablecimientoClienteDTO establecimiento) {
        if (servicio.getEstablecimientoNombre() == null || servicio.getEstablecimientoNombre().trim().isEmpty()) {
            servicio.setEstablecimientoNombre(establecimiento.getNombre());
        }
        
        if (servicio.getEstablecimientoId() == null) {
            servicio.setEstablecimientoId(establecimiento.getId());
        }
        
        // Asegurar que tenga una descripción
        if (servicio.getDescripcion() == null || servicio.getDescripcion().trim().isEmpty()) {
            servicio.setDescripcion("Servicio profesional de calidad en " + establecimiento.getNombre());
        }
        
        // Asegurar duración mínima
        if (servicio.getDuracionMinutos() == null || servicio.getDuracionMinutos() <= 0) {
            servicio.setDuracionMinutos(30);
        }
        
        return servicio;
    }
    
    /**
     * Calcular estadísticas generales
     */
    private Map<String, Object> calcularEstadisticas(
            List<EstablecimientoClienteDTO> establecimientos,
            Map<String, List<ServicioClienteDTO>> serviciosPorCategoria) {
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            int totalEstablecimientos = establecimientos != null ? establecimientos.size() : 0;
            int totalServicios = serviciosPorCategoria != null ? 
                serviciosPorCategoria.values().stream()
                    .mapToInt(List::size)
                    .sum() : 0;
            
            // Calcular calificación promedio real
            double calificacionPromedio = 4.5; // Valor por defecto
            if (establecimientos != null && !establecimientos.isEmpty()) {
                calificacionPromedio = establecimientos.stream()
                    .filter(est -> est.getCalificacionPromedio() > 0)
                    .mapToDouble(EstablecimientoClienteDTO::getCalificacionPromedio)
                    .average()
                    .orElse(4.5);
            }
            
            estadisticas.put("totalEstablecimientos", totalEstablecimientos);
            estadisticas.put("totalServicios", totalServicios);
            estadisticas.put("calificacionPromedio", Math.round(calificacionPromedio * 10.0) / 10.0);
            
        } catch (Exception e) {
            System.err.println("Error calculando estadísticas: " + e.getMessage());
            // Valores por defecto
            estadisticas.put("totalEstablecimientos", 0);
            estadisticas.put("totalServicios", 0);
            estadisticas.put("calificacionPromedio", 4.5);
        }
        
        return estadisticas;
    }
    
    /**
     * Cargar datos por defecto en caso de error completo
     */
    private void cargarDatosPorDefecto(Model model) {
        model.addAttribute("establecimientosDestacados", new ArrayList<>());
        model.addAttribute("serviciosPorCategoria", new HashMap<>());
        
        Map<String, Object> estadisticasDefault = new HashMap<>();
        estadisticasDefault.put("totalEstablecimientos", 0);
        estadisticasDefault.put("totalServicios", 0);
        estadisticasDefault.put("calificacionPromedio", 4.5);
        model.addAttribute("estadisticasGenerales", estadisticasDefault);
        
        System.out.println("Datos por defecto cargados debido a errores en el sistema");
    }
}
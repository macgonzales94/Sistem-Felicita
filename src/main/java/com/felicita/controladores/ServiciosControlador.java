package com.felicita.controladores;

import com.felicita.dto.EstablecimientoClienteDTO;
import com.felicita.dto.ServicioClienteDTO;
import com.felicita.servicios.EstablecimientoClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/servicios")
public class ServiciosControlador {

    @Autowired
    private EstablecimientoClienteServicio establecimientoClienteServicio;

    /**
     * Mostrar la página principal de servicios
     */
    @GetMapping
    public String servicios(Model model,
                           @RequestParam(defaultValue = "todos") String categoria,
                           @RequestParam(required = false) String busqueda,
                           @RequestParam(required = false) String ubicacion) {
        
        // Agregar parámetros al modelo para mantener el estado
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("busquedaActual", busqueda != null ? busqueda : "");
        model.addAttribute("ubicacionSeleccionada", ubicacion != null ? ubicacion : "");
        
        // Obtener estadísticas básicas para mostrar en la página
        model.addAttribute("totalEstablecimientos", this.obtenerConteoEstablecimientos());
        
        return "servicios";
    }

    /**
     * API para obtener todos los servicios con filtros y paginación
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerServicios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "todos") String categoria,
            @RequestParam(required = false) String subcategoria,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Double calificacionMin,
            @RequestParam(defaultValue = "relevancia") String ordenarPor) {
        
        try {
            // Obtener establecimientos según filtros
            List<EstablecimientoClienteDTO> establecimientos = obtenerEstablecimientosFiltrados(
                categoria, busqueda, ubicacion, calificacionMin);
            
            // Obtener servicios reales de cada establecimiento
            List<ServicioClienteDTO> todosLosServicios = new ArrayList<>();
            for (EstablecimientoClienteDTO establecimiento : establecimientos) {
                try {
                    List<ServicioClienteDTO> serviciosEst = 
                        establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(establecimiento.getId());
                    
                    // Filtrar servicios disponibles
                    List<ServicioClienteDTO> serviciosDisponibles = serviciosEst.stream()
                        .filter(ServicioClienteDTO::isDisponible)
                        .collect(Collectors.toList());
                    
                    todosLosServicios.addAll(serviciosDisponibles);
                } catch (Exception e) {
                    System.err.println("Error obteniendo servicios del establecimiento " + establecimiento.getId() + ": " + e.getMessage());
                    // Continuar con el siguiente establecimiento
                }
            }
            
            // Aplicar filtros adicionales
            List<ServicioClienteDTO> serviciosFiltrados = aplicarFiltrosAdicionales(
                todosLosServicios, precioMin, precioMax, subcategoria);
            
            // Ordenar servicios
            serviciosFiltrados = ordenarServicios(serviciosFiltrados, ordenarPor);
            
            // Aplicar paginación manual
            int inicio = page * size;
            int fin = Math.min(inicio + size, serviciosFiltrados.size());
            
            List<ServicioClienteDTO> serviciosPaginados = new ArrayList<>();
            if (inicio < serviciosFiltrados.size()) {
                serviciosPaginados = serviciosFiltrados.subList(inicio, fin);
            }
            
            // Convertir a formato para frontend
            List<Map<String, Object>> serviciosResponse = serviciosPaginados.stream()
                .map(this::convertirServicioAMap)
                .collect(Collectors.toList());
            
            // Calcular información de paginación
            int totalElementos = serviciosFiltrados.size();
            int totalPaginas = (int) Math.ceil((double) totalElementos / size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("servicios", serviciosResponse);
            response.put("totalElementos", totalElementos);
            response.put("totalPaginas", totalPaginas);
            response.put("paginaActual", page);
            response.put("tamanoPagina", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error en obtenerServicios: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener servicios: " + e.getMessage());
            errorResponse.put("servicios", new ArrayList<>());
            errorResponse.put("totalElementos", 0);
            errorResponse.put("totalPaginas", 0);
            errorResponse.put("paginaActual", 0);
            errorResponse.put("tamanoPagina", size);
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * API para obtener servicios por categoría específica
     */
    @GetMapping("/api/categoria/{categoria}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerServiciosPorCategoria(
            @PathVariable String categoria,
            @RequestParam(required = false) String subcategoria) {
        
        try {
            List<EstablecimientoClienteDTO> establecimientos;
            
            switch (categoria.toLowerCase()) {
                case "barberia":
                    establecimientos = establecimientoClienteServicio.buscarPorTipo("BARBERIA");
                    break;
                case "belleza":
                    establecimientos = establecimientoClienteServicio.buscarPorTipo("SALON_BELLEZA");
                    break;
                case "giftcard":
                    // Para gift cards, mostrar todos los establecimientos
                    Pageable pageable = PageRequest.of(0, 100);
                    Page<EstablecimientoClienteDTO> pageEstablecimientos = 
                        establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
                    establecimientos = pageEstablecimientos.getContent();
                    break;
                default:
                    Pageable pageableDefault = PageRequest.of(0, 100);
                    Page<EstablecimientoClienteDTO> pageDefault = 
                        establecimientoClienteServicio.obtenerEstablecimientosActivos(pageableDefault);
                    establecimientos = pageDefault.getContent();
            }
            
            // Obtener servicios reales
            List<ServicioClienteDTO> todosLosServicios = new ArrayList<>();
            for (EstablecimientoClienteDTO establecimiento : establecimientos) {
                try {
                    List<ServicioClienteDTO> serviciosEst = 
                        establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(establecimiento.getId());
                    
                    // Filtrar por categoría si es necesario
                    List<ServicioClienteDTO> serviciosFiltrados = serviciosEst.stream()
                        .filter(ServicioClienteDTO::isDisponible)
                        .collect(Collectors.toList());
                    
                    // Para gift cards, generar gift cards simuladas ya que no hay entidad específica
                    if ("giftcard".equals(categoria.toLowerCase())) {
                        serviciosFiltrados = generarGiftCardsParaEstablecimiento(establecimiento);
                    }
                    
                    todosLosServicios.addAll(serviciosFiltrados);
                } catch (Exception e) {
                    System.err.println("Error obteniendo servicios del establecimiento " + establecimiento.getId());
                }
            }
            
            // Filtrar por subcategoría si se especifica
            if (subcategoria != null && !subcategoria.isEmpty()) {
                todosLosServicios = todosLosServicios.stream()
                    .filter(servicio -> subcategoria.equals(servicio.getCategoria()))
                    .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> serviciosResponse = todosLosServicios.stream()
                .map(this::convertirServicioAMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(serviciosResponse);
            
        } catch (Exception e) {
            System.err.println("Error en obtenerServiciosPorCategoria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API para buscar servicios
     */
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> buscarServicios(
            @RequestParam String termino,
            @RequestParam(required = false) String ubicacion) {
        
        try {
            List<EstablecimientoClienteDTO> establecimientos;
            
            if (ubicacion != null && !ubicacion.isEmpty()) {
                establecimientos = establecimientoClienteServicio.buscarPorCiudad(ubicacion);
            } else {
                establecimientos = establecimientoClienteServicio.buscarPorNombre(termino);
            }
            
            // Obtener servicios reales
            List<ServicioClienteDTO> todosLosServicios = new ArrayList<>();
            for (EstablecimientoClienteDTO establecimiento : establecimientos) {
                try {
                    List<ServicioClienteDTO> serviciosEst = 
                        establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(establecimiento.getId());
                    todosLosServicios.addAll(serviciosEst);
                } catch (Exception e) {
                    System.err.println("Error obteniendo servicios para búsqueda del establecimiento " + establecimiento.getId());
                }
            }
            
            // Filtrar servicios por término de búsqueda
            List<ServicioClienteDTO> serviciosFiltrados = todosLosServicios.stream()
                .filter(servicio -> 
                    servicio.getNombre().toLowerCase().contains(termino.toLowerCase()) ||
                    (servicio.getDescripcion() != null && servicio.getDescripcion().toLowerCase().contains(termino.toLowerCase())) ||
                    servicio.getEstablecimientoNombre().toLowerCase().contains(termino.toLowerCase())
                )
                .filter(ServicioClienteDTO::isDisponible)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> serviciosResponse = serviciosFiltrados.stream()
                .map(this::convertirServicioAMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(serviciosResponse);
            
        } catch (Exception e) {
            System.err.println("Error en buscarServicios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API para obtener establecimientos para el formulario de reserva
     */
    @GetMapping("/api/establecimientos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerEstablecimientos(
            @RequestParam(required = false) String categoria) {
        
        try {
            List<EstablecimientoClienteDTO> establecimientos;
            
            if (categoria != null && !categoria.equals("todos")) {
                switch (categoria.toLowerCase()) {
                    case "barberia":
                        establecimientos = establecimientoClienteServicio.buscarPorTipo("BARBERIA");
                        break;
                    case "belleza":
                        establecimientos = establecimientoClienteServicio.buscarPorTipo("SALON_BELLEZA");
                        break;
                    default:
                        Pageable pageable = PageRequest.of(0, 100);
                        Page<EstablecimientoClienteDTO> pageEstablecimientos = 
                            establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
                        establecimientos = pageEstablecimientos.getContent();
                }
            } else {
                Pageable pageable = PageRequest.of(0, 100);
                Page<EstablecimientoClienteDTO> pageEstablecimientos = 
                    establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
                establecimientos = pageEstablecimientos.getContent();
            }
            
            List<Map<String, Object>> resultado = establecimientos.stream()
                .map(est -> {
                    Map<String, Object> establecimientoMap = new HashMap<>();
                    establecimientoMap.put("id", est.getId());
                    establecimientoMap.put("nombre", est.getNombre());
                    establecimientoMap.put("ubicacion", est.getCiudad());
                    establecimientoMap.put("tipo", est.getTipo());
                    return establecimientoMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("Error en obtenerEstablecimientos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API para obtener servicios de un establecimiento específico
     */
    @GetMapping("/api/establecimientos/{id}/servicios")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerServiciosEstablecimiento(@PathVariable Long id) {
        
        try {
            List<ServicioClienteDTO> servicios = 
                establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(id);
            
            List<Map<String, Object>> resultado = servicios.stream()
                .filter(ServicioClienteDTO::isDisponible)
                .map(servicio -> {
                    Map<String, Object> servicioMap = new HashMap<>();
                    servicioMap.put("id", servicio.getId());
                    servicioMap.put("nombre", servicio.getNombre());
                    servicioMap.put("descripcion", servicio.getDescripcion());
                    servicioMap.put("precio", servicio.getPrecio());
                    servicioMap.put("duracion", servicio.getDuracionMinutos());
                    servicioMap.put("categoria", servicio.getCategoria());
                    servicioMap.put("disponible", servicio.isDisponible());
                    return servicioMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("Error en obtenerServiciosEstablecimiento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API para obtener horarios disponibles
     */
    @GetMapping("/api/establecimientos/{id}/horarios")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(
            @PathVariable Long id,
            @RequestParam String fecha) {
        
        try {
            List<String> horarios = establecimientoClienteServicio.obtenerHorariosDisponibles(id, fecha);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            System.err.println("Error en obtenerHorariosDisponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Obtener establecimientos filtrados según criterios
     */
    private List<EstablecimientoClienteDTO> obtenerEstablecimientosFiltrados(
            String categoria, String busqueda, String ubicacion, Double calificacionMin) {
        
        try {
            List<EstablecimientoClienteDTO> establecimientos;
            
            // Filtrar por categoría
            if (!categoria.equals("todos")) {
                switch (categoria.toLowerCase()) {
                    case "barberia":
                        establecimientos = establecimientoClienteServicio.buscarPorTipo("BARBERIA");
                        break;
                    case "belleza":
                        establecimientos = establecimientoClienteServicio.buscarPorTipo("SALON_BELLEZA");
                        break;
                    case "giftcard":
                        Pageable pageable = PageRequest.of(0, 100);
                        Page<EstablecimientoClienteDTO> pageEstablecimientos = 
                            establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
                        establecimientos = pageEstablecimientos.getContent();
                        break;
                    default:
                        Pageable pageableDefault = PageRequest.of(0, 100);
                        Page<EstablecimientoClienteDTO> pageDefault = 
                            establecimientoClienteServicio.obtenerEstablecimientosActivos(pageableDefault);
                        establecimientos = pageDefault.getContent();
                }
            } else {
                Pageable pageable = PageRequest.of(0, 100);
                Page<EstablecimientoClienteDTO> pageEstablecimientos = 
                    establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
                establecimientos = pageEstablecimientos.getContent();
            }
            
            // Aplicar filtros adicionales
            return establecimientos.stream()
                .filter(est -> {
                    // Filtro por búsqueda
                    if (busqueda != null && !busqueda.isEmpty()) {
                        String busquedaLower = busqueda.toLowerCase();
                        boolean coincide = est.getNombre().toLowerCase().contains(busquedaLower) ||
                                         (est.getDescripcion() != null && est.getDescripcion().toLowerCase().contains(busquedaLower)) ||
                                         est.getCiudad().toLowerCase().contains(busquedaLower);
                        if (!coincide) return false;
                    }
                    
                    // Filtro por ubicación
                    if (ubicacion != null && !ubicacion.isEmpty()) {
                        if (!est.getCiudad().toLowerCase().contains(ubicacion.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // Filtro por calificación mínima
                    if (calificacionMin != null) {
                        if (est.getCalificacionPromedio() < calificacionMin) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Error en obtenerEstablecimientosFiltrados: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Aplicar filtros adicionales a los servicios
     */
    private List<ServicioClienteDTO> aplicarFiltrosAdicionales(
            List<ServicioClienteDTO> servicios, Double precioMin, Double precioMax, String subcategoria) {
        
        return servicios.stream()
            .filter(servicio -> {
                // Filtro por precio mínimo
                if (precioMin != null) {
                    if (servicio.getPrecio().doubleValue() < precioMin) return false;
                }
                
                // Filtro por precio máximo
                if (precioMax != null) {
                    if (servicio.getPrecio().doubleValue() > precioMax) return false;
                }
                
                // Filtro por subcategoría
                if (subcategoria != null && !subcategoria.isEmpty()) {
                    if (!subcategoria.equals(servicio.getCategoria())) return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }

    /**
     * Ordenar servicios según criterio
     */
    private List<ServicioClienteDTO> ordenarServicios(List<ServicioClienteDTO> servicios, String ordenarPor) {
        switch (ordenarPor.toLowerCase()) {
            case "precio-asc":
                return servicios.stream()
                    .sorted((a, b) -> a.getPrecio().compareTo(b.getPrecio()))
                    .collect(Collectors.toList());
            case "precio-desc":
                return servicios.stream()
                    .sorted((a, b) -> b.getPrecio().compareTo(a.getPrecio()))
                    .collect(Collectors.toList());
            case "calificacion":
                return servicios.stream()
                    .sorted((a, b) -> b.getEstablecimientoNombre().compareTo(a.getEstablecimientoNombre()))
                    .collect(Collectors.toList());
            case "distancia":
                return servicios.stream()
                    .sorted((a, b) -> a.getEstablecimientoNombre().compareToIgnoreCase(b.getEstablecimientoNombre()))
                    .collect(Collectors.toList());
            default:
                return servicios; // Relevancia = orden actual
        }
    }

    /**
     * Convertir ServicioClienteDTO a Map para respuesta JSON
     */
    private Map<String, Object> convertirServicioAMap(ServicioClienteDTO servicio) {
        Map<String, Object> servicioMap = new HashMap<>();
        servicioMap.put("id", servicio.getId());
        servicioMap.put("nombre", servicio.getNombre());
        servicioMap.put("descripcion", servicio.getDescripcion() != null ? servicio.getDescripcion() : "Servicio profesional de calidad");
        servicioMap.put("precio", servicio.getPrecio().doubleValue());
        servicioMap.put("categoria", servicio.getCategoria());
        servicioMap.put("subcategoria", servicio.getCategoria());
        servicioMap.put("establecimiento", servicio.getEstablecimientoNombre());
        servicioMap.put("ubicacion", "Lima"); // Por defecto, podrías obtenerlo del establecimiento
        servicioMap.put("calificacion", 4.5); // Valor por defecto, podrías calcularlo
        servicioMap.put("numeroResenas", 15); // Valor por defecto
        servicioMap.put("imagen", servicio.getImagenUrl() != null ? servicio.getImagenUrl() : "/img/servicios/default.jpg");
        servicioMap.put("disponible", servicio.isDisponible());
        servicioMap.put("duracion", servicio.getDuracionMinutos());
        servicioMap.put("establecimientoId", servicio.getEstablecimientoId());
        
        return servicioMap;
    }

    /**
     * Generar gift cards para un establecimiento (solo cuando sea necesario)
     */
    private List<ServicioClienteDTO> generarGiftCardsParaEstablecimiento(EstablecimientoClienteDTO establecimiento) {
        List<ServicioClienteDTO> giftCards = new ArrayList<>();
        
        // Gift Card 50 soles
        ServicioClienteDTO gift50 = new ServicioClienteDTO();
        gift50.setId(establecimiento.getId() * 1000 + 50L);
        gift50.setNombre("Gift Card S/ 50");
        gift50.setDescripcion("Tarjeta regalo de S/ 50 para " + establecimiento.getNombre());
        gift50.setPrecio(new java.math.BigDecimal("50.00"));
        gift50.setDuracionMinutos(0);
        gift50.setCategoria("Gift Card");
        gift50.setEstablecimientoId(establecimiento.getId());
        gift50.setEstablecimientoNombre(establecimiento.getNombre());
        gift50.setDisponible(true);
        giftCards.add(gift50);
        
        // Gift Card 100 soles
        ServicioClienteDTO gift100 = new ServicioClienteDTO();
        gift100.setId(establecimiento.getId() * 1000 + 100L);
        gift100.setNombre("Gift Card S/ 100");
        gift100.setDescripcion("Tarjeta regalo de S/ 100 para " + establecimiento.getNombre());
        gift100.setPrecio(new java.math.BigDecimal("100.00"));
        gift100.setDuracionMinutos(0);
        gift100.setCategoria("Gift Card");
        gift100.setEstablecimientoId(establecimiento.getId());
        gift100.setEstablecimientoNombre(establecimiento.getNombre());
        gift100.setDisponible(true);
        giftCards.add(gift100);
        
        return giftCards;
    }

    /**
     * Obtener conteo básico de establecimientos para estadísticas
     */
    private Map<String, Integer> obtenerConteoEstablecimientos() {
        Map<String, Integer> conteo = new HashMap<>();
        
        try {
            // Obtener conteos por tipo usando los servicios existentes
            List<EstablecimientoClienteDTO> barberias = establecimientoClienteServicio.buscarPorTipo("BARBERIA");
            List<EstablecimientoClienteDTO> salones = establecimientoClienteServicio.buscarPorTipo("SALON_BELLEZA");
            
            conteo.put("barberias", barberias.size());
            conteo.put("salones", salones.size());
            conteo.put("total", barberias.size() + salones.size());
            
        } catch (Exception e) {
            // Valores por defecto en caso de error
            conteo.put("barberias", 0);
            conteo.put("salones", 0);
            conteo.put("total", 0);
        }
        
        return conteo;
    }
}
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
            Pageable pageable = crearPageable(page, size, ordenarPor);
            
            // Obtener establecimientos activos
            Page<EstablecimientoClienteDTO> establecimientos = 
                establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
            
            // Aplicar filtros
            List<EstablecimientoClienteDTO> establecimientosFiltrados = 
                aplicarFiltros(establecimientos.getContent(), categoria, busqueda, ubicacion, 
                              precioMin, precioMax, calificacionMin);
            
            // Convertir a servicios
            List<Map<String, Object>> servicios = convertirAServicios(establecimientosFiltrados);
            
            Map<String, Object> response = new HashMap<>();
            response.put("servicios", servicios);
            response.put("totalElementos", servicios.size());
            response.put("totalPaginas", Math.ceil((double) servicios.size() / size));
            response.put("paginaActual", page);
            response.put("tamanoPagina", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener servicios: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
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
                    establecimientos = establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable).getContent();
                    break;
                default:
                    Pageable pageableDefault = PageRequest.of(0, 100);
                    establecimientos = establecimientoClienteServicio.obtenerEstablecimientosActivos(pageableDefault).getContent();
            }
            
            List<Map<String, Object>> servicios = convertirAServicios(establecimientos);
            
            // Filtrar por subcategoría si se especifica
            if (subcategoria != null && !subcategoria.isEmpty()) {
                servicios = servicios.stream()
                    .filter(servicio -> subcategoria.equals(servicio.get("subcategoria")))
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(servicios);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
            
            List<Map<String, Object>> servicios = convertirAServicios(establecimientos);
            
            // Filtrar servicios por término de búsqueda
            servicios = servicios.stream()
                .filter(servicio -> 
                    servicio.get("nombre").toString().toLowerCase().contains(termino.toLowerCase()) ||
                    servicio.get("descripcion").toString().toLowerCase().contains(termino.toLowerCase()) ||
                    servicio.get("establecimiento").toString().toLowerCase().contains(termino.toLowerCase())
                )
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(servicios);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
                        establecimientos = establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable).getContent();
                }
            } else {
                Pageable pageable = PageRequest.of(0, 100);
                establecimientos = establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable).getContent();
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
            return ResponseEntity.badRequest().build();
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
            return ResponseEntity.badRequest().build();
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
            return ResponseEntity.badRequest().build();
        }
    }

    // Métodos privados para procesamiento de datos

    /**
     * Crear objeto Pageable según criterio de ordenamiento
     */
    private Pageable crearPageable(int page, int size, String ordenarPor) {
        Sort sort;
        
        switch (ordenarPor.toLowerCase()) {
            case "precio-asc":
                sort = Sort.by("nombre").ascending(); // Simular ordenamiento por precio
                break;
            case "precio-desc":
                sort = Sort.by("nombre").descending();
                break;
            case "calificacion":
                sort = Sort.by("calificacionPromedio").descending();
                break;
            case "distancia":
                sort = Sort.by("ciudad").ascending();
                break;
            default:
                sort = Sort.by("nombre").ascending();
        }
        
        return PageRequest.of(page, size, sort);
    }

    /**
     * Aplicar filtros a la lista de establecimientos
     */
    private List<EstablecimientoClienteDTO> aplicarFiltros(
            List<EstablecimientoClienteDTO> establecimientos,
            String categoria, String busqueda, String ubicacion,
            Double precioMin, Double precioMax, Double calificacionMin) {
        
        return establecimientos.stream()
            .filter(est -> {
                // Filtro por categoría
                if (!categoria.equals("todos")) {
                    switch (categoria.toLowerCase()) {
                        case "barberia":
                            if (!"BARBERIA".equals(est.getTipo())) return false;
                            break;
                        case "belleza":
                            if (!"SALON_BELLEZA".equals(est.getTipo())) return false;
                            break;
                        case "giftcard":
                            // Para gift cards, incluir todos los tipos
                            break;
                    }
                }
                
                // Filtro por búsqueda
                if (busqueda != null && !busqueda.isEmpty()) {
                    String busquedaLower = busqueda.toLowerCase();
                    boolean coincide = est.getNombre().toLowerCase().contains(busquedaLower) ||
                                     est.getDescripcion().toLowerCase().contains(busquedaLower) ||
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
    }

    /**
     * Convertir establecimientos a formato de servicios para la respuesta
     */
    private List<Map<String, Object>> convertirAServicios(List<EstablecimientoClienteDTO> establecimientos) {
        return establecimientos.stream()
            .flatMap(est -> this.generarServiciosDeEstablecimiento(est).stream())
            .collect(Collectors.toList());
    }

    /**
     * Generar servicios simulados para un establecimiento
     */
    private List<Map<String, Object>> generarServiciosDeEstablecimiento(EstablecimientoClienteDTO establecimiento) {
        List<Map<String, Object>> servicios = new java.util.ArrayList<>();
        
        if ("BARBERIA".equals(establecimiento.getTipo())) {
            servicios.addAll(generarServiciosBarberia(establecimiento));
        } else if ("SALON_BELLEZA".equals(establecimiento.getTipo())) {
            servicios.addAll(generarServiciosBelleza(establecimiento));
        }
        
        // Agregar gift cards para todos los establecimientos
        servicios.addAll(generarGiftCards(establecimiento));
        
        return servicios;
    }

    /**
     * Generar servicios específicos de barbería
     */
    private List<Map<String, Object>> generarServiciosBarberia(EstablecimientoClienteDTO establecimiento) {
        List<Map<String, Object>> servicios = new java.util.ArrayList<>();
        
        // Corte de cabello
        Map<String, Object> corte = new HashMap<>();
        corte.put("id", establecimiento.getId() * 100 + 1);
        corte.put("nombre", "Corte de Cabello Moderno");
        corte.put("descripcion", "Corte profesional con las últimas tendencias");
        corte.put("precio", 25.00);
        corte.put("precioOriginal", 35.00);
        corte.put("categoria", "barberia");
        corte.put("subcategoria", "corte-cabello");
        corte.put("establecimiento", establecimiento.getNombre());
        corte.put("ubicacion", establecimiento.getCiudad());
        corte.put("calificacion", establecimiento.getCalificacionPromedio());
        corte.put("numeroResenas", establecimiento.getCantidadResenas());
        corte.put("imagen", "/img/servicios/corte-moderno.jpg");
        corte.put("descuento", 30);
        corte.put("disponible", true);
        corte.put("duracion", 45);
        servicios.add(corte);
        
        // Arreglo de barba
        Map<String, Object> barba = new HashMap<>();
        barba.put("id", establecimiento.getId() * 100 + 2);
        barba.put("nombre", "Arreglo de Barba Profesional");
        barba.put("descripcion", "Arreglo y diseño de barba con técnicas profesionales");
        barba.put("precio", 20.00);
        barba.put("categoria", "barberia");
        barba.put("subcategoria", "arreglo-barba");
        barba.put("establecimiento", establecimiento.getNombre());
        barba.put("ubicacion", establecimiento.getCiudad());
        barba.put("calificacion", establecimiento.getCalificacionPromedio());
        barba.put("numeroResenas", establecimiento.getCantidadResenas());
        barba.put("imagen", "/img/servicios/arreglo-barba.jpg");
        barba.put("disponible", true);
        barba.put("duracion", 30);
        servicios.add(barba);
        
        // Corte y barba combo
        Map<String, Object> combo = new HashMap<>();
        combo.put("id", establecimiento.getId() * 100 + 3);
        combo.put("nombre", "Corte + Barba Combo");
        combo.put("descripcion", "Servicio completo de corte de cabello y arreglo de barba");
        combo.put("precio", 40.00);
        combo.put("precioOriginal", 50.00);
        combo.put("categoria", "barberia");
        combo.put("subcategoria", "corte-barba");
        combo.put("establecimiento", establecimiento.getNombre());
        combo.put("ubicacion", establecimiento.getCiudad());
        combo.put("calificacion", establecimiento.getCalificacionPromedio());
        combo.put("numeroResenas", establecimiento.getCantidadResenas());
        combo.put("imagen", "/img/servicios/corte-barba-combo.jpg");
        combo.put("descuento", 20);
        combo.put("disponible", true);
        combo.put("duracion", 75);
        servicios.add(combo);
        
        return servicios;
    }

    /**
     * Generar servicios específicos de salón de belleza
     */
    private List<Map<String, Object>> generarServiciosBelleza(EstablecimientoClienteDTO establecimiento) {
        List<Map<String, Object>> servicios = new java.util.ArrayList<>();
        
        // Tratamiento facial
        Map<String, Object> facial = new HashMap<>();
        facial.put("id", establecimiento.getId() * 100 + 10);
        facial.put("nombre", "Tratamiento Facial Hidratante");
        facial.put("descripcion", "Tratamiento facial completo con productos premium");
        facial.put("precio", 80.00);
        facial.put("categoria", "belleza");
        facial.put("subcategoria", "tratamientos-faciales");
        facial.put("establecimiento", establecimiento.getNombre());
        facial.put("ubicacion", establecimiento.getCiudad());
        facial.put("calificacion", establecimiento.getCalificacionPromedio());
        facial.put("numeroResenas", establecimiento.getCantidadResenas());
        facial.put("imagen", "/img/servicios/facial-hidratante.jpg");
        facial.put("disponible", true);
        facial.put("duracion", 90);
        servicios.add(facial);
        
        // Manicure
        Map<String, Object> manicure = new HashMap<>();
        manicure.put("id", establecimiento.getId() * 100 + 11);
        manicure.put("nombre", "Manicure Gel con Diseño");
        manicure.put("descripcion", "Manicure profesional con esmalte gel y diseños personalizados");
        manicure.put("precio", 35.00);
        manicure.put("categoria", "belleza");
        manicure.put("subcategoria", "manicure");
        manicure.put("establecimiento", establecimiento.getNombre());
        manicure.put("ubicacion", establecimiento.getCiudad());
        manicure.put("calificacion", establecimiento.getCalificacionPromedio());
        manicure.put("numeroResenas", establecimiento.getCantidadResenas());
        manicure.put("imagen", "/img/servicios/manicure-gel.jpg");
        manicure.put("disponible", true);
        manicure.put("duracion", 60);
        servicios.add(manicure);
        
        // Coloración
        Map<String, Object> coloracion = new HashMap<>();
        coloracion.put("id", establecimiento.getId() * 100 + 12);
        coloracion.put("nombre", "Coloración Completa");
        coloracion.put("descripcion", "Cambio de color completo con productos de alta calidad");
        coloracion.put("precio", 120.00);
        coloracion.put("precioOriginal", 150.00);
        coloracion.put("categoria", "belleza");
        coloracion.put("subcategoria", "coloracion");
        coloracion.put("establecimiento", establecimiento.getNombre());
        coloracion.put("ubicacion", establecimiento.getCiudad());
        coloracion.put("calificacion", establecimiento.getCalificacionPromedio());
        coloracion.put("numeroResenas", establecimiento.getCantidadResenas());
        coloracion.put("imagen", "/img/servicios/coloracion.jpg");
        coloracion.put("descuento", 20);
        coloracion.put("disponible", true);
        coloracion.put("duracion", 180);
        servicios.add(coloracion);
        
        return servicios;
    }

    /**
     * Generar gift cards para un establecimiento
     */
    private List<Map<String, Object>> generarGiftCards(EstablecimientoClienteDTO establecimiento) {
        List<Map<String, Object>> giftCards = new java.util.ArrayList<>();
        
        // Gift Card de S/ 50
        Map<String, Object> gift50 = new HashMap<>();
        gift50.put("id", establecimiento.getId() * 100 + 50);
        gift50.put("nombre", "Gift Card - S/ 50");
        gift50.put("descripcion", "Tarjeta de regalo de S/ 50 válida en " + establecimiento.getNombre());
        gift50.put("precio", 50.00);
        gift50.put("categoria", "giftcard");
        gift50.put("subcategoria", "BARBERIA".equals(establecimiento.getTipo()) ? "giftcard-barberia" : "giftcard-belleza");
        gift50.put("establecimiento", establecimiento.getNombre());
        gift50.put("ubicacion", establecimiento.getCiudad());
        gift50.put("calificacion", 5.0);
        gift50.put("numeroResenas", 25);
        gift50.put("imagen", "/img/servicios/giftcard-50.jpg");
        gift50.put("disponible", true);
        gift50.put("validez", "12 meses");
        giftCards.add(gift50);
        
        // Gift Card de S/ 100
        Map<String, Object> gift100 = new HashMap<>();
        gift100.put("id", establecimiento.getId() * 100 + 51);
        gift100.put("nombre", "Gift Card - S/ 100");
        gift100.put("descripcion", "Tarjeta de regalo de S/ 100 válida en " + establecimiento.getNombre());
        gift100.put("precio", 100.00);
        gift100.put("categoria", "giftcard");
        gift100.put("subcategoria", "BARBERIA".equals(establecimiento.getTipo()) ? "giftcard-barberia" : "giftcard-belleza");
        gift100.put("establecimiento", establecimiento.getNombre());
        gift100.put("ubicacion", establecimiento.getCiudad());
        gift100.put("calificacion", 5.0);
        gift100.put("numeroResenas", 45);
        gift100.put("imagen", "/img/servicios/giftcard-100.jpg");
        gift100.put("disponible", true);
        gift100.put("validez", "12 meses");
        giftCards.add(gift100);
        
        return giftCards;
    }

    /**
     * Obtener conteo básico de establecimientos para estadísticas
     */
    private Map<String, Integer> obtenerConteoEstablecimientos() {
        Map<String, Integer> conteo = new HashMap<>();
        
        try {
            // Obtener conteos por tipo
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
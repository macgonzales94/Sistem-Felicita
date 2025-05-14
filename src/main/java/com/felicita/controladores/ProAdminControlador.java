package com.felicita.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proadmin")
public class ProAdminControlador {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "proadmin/dashboard";
    }
    
    @GetMapping("/establecimiento")
    public String perfilEstablecimiento(Model model) {
        return "proadmin/perfil-establecimiento";
    }
    
    @GetMapping("/servicios")
    public String servicios(Model model) {
        return "proadmin/servicios";
    }
    
    @GetMapping("/citas")
    public String citas(Model model) {
        return "proadmin/citas";
    }
    
    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        return "proadmin/estadisticas";
    }
}
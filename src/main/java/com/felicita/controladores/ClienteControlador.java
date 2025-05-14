package com.felicita.controladores;

import com.felicita.dto.ClienteDTO;
import com.felicita.servicios.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class ClienteControlador {

    @Autowired
    private ClienteServicio clienteServicio;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        ClienteDTO cliente = clienteServicio.obtenerClienteAutenticado();
        model.addAttribute("cliente", cliente);
        return "cliente/inicio";
    }
    
    @GetMapping("/perfil")
    public String perfil(Model model) {
        ClienteDTO cliente = clienteServicio.obtenerClienteAutenticado();
        model.addAttribute("cliente", cliente);
        model.addAttribute("editarPerfilForm", cliente);
        return "cliente/perfil";
    }
    
    @PostMapping("/perfil")
    public String actualizarPerfil(@Valid @ModelAttribute("editarPerfilForm") ClienteDTO clienteDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cliente/perfil";
        }
        
        ClienteDTO clienteActualizado = clienteServicio.actualizarCliente(clienteDTO.getId(), clienteDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
        return "redirect:/cliente/perfil";
    }
    
    @GetMapping("/establecimientos")
    public String listarEstablecimientos(Model model) {
        return "cliente/establecimientos";
    }
    
    @GetMapping("/establecimientos/{id}")
    public String detalleEstablecimiento(@PathVariable Long id, Model model) {
        return "cliente/detalle-establecimiento";
    }
    /*
    @GetMapping("/giftcards")
    public String giftcards(Model model) {
        return "cliente/giftcards";
    }
    */
    
    // API REST endpoints para acceso desde JavaScript
    
    @GetMapping("/api/perfil")
    @ResponseBody
    public ResponseEntity<ClienteDTO> obtenerPerfilApi() {
        ClienteDTO cliente = clienteServicio.obtenerClienteAutenticado();
        return ResponseEntity.ok(cliente);
    }
    
    @PutMapping("/api/perfil")
    @ResponseBody
    public ResponseEntity<ClienteDTO> actualizarPerfilApi(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO clienteActualizado = clienteServicio.actualizarCliente(clienteDTO.getId(), clienteDTO);
        return ResponseEntity.ok(clienteActualizado);
    }
}
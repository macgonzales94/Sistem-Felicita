package com.felicita.controladores;

import com.felicita.dto.GiftCardDTO;
import com.felicita.servicios.GiftCardServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente/giftcards")
public class GiftCardControlador {

    @Autowired
    private GiftCardServicio giftCardServicio;
    
    @GetMapping
    public String listarGiftCards(Model model) {
        List<GiftCardDTO> giftCards = giftCardServicio.obtenerGiftCardsCliente();
        model.addAttribute("giftCards", giftCards);
        model.addAttribute("nuevaGiftCardForm", new GiftCardDTO());
        
        return "cliente/giftcards";
    }
    
    @GetMapping("/{id}")
    public String detalleGiftCard(@PathVariable Long id, Model model) {
        GiftCardDTO giftCard = giftCardServicio.obtenerGiftCardPorId(id);
        model.addAttribute("giftCard", giftCard);
        
        return "cliente/detalle-giftcard";
    }
    
    @PostMapping
    public String crearGiftCard(@Valid @ModelAttribute("nuevaGiftCardForm") GiftCardDTO giftCardDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cliente/giftcards";
        }
        
        try {
            GiftCardDTO giftCardCreada = giftCardServicio.crearGiftCard(giftCardDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Gift Card creada correctamente. Código: " + giftCardCreada.getCodigo());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cliente/giftcards";
    }
    
    @PostMapping("/{id}/activar")
    public String activarGiftCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            GiftCardDTO giftCardActivada = giftCardServicio.activarGiftCard(id);
            redirectAttributes.addFlashAttribute("mensaje", "Gift Card activada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cliente/giftcards";
    }
    
    @PostMapping("/{id}/desactivar")
    public String desactivarGiftCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            GiftCardDTO giftCardDesactivada = giftCardServicio.desactivarGiftCard(id);
            redirectAttributes.addFlashAttribute("mensaje", "Gift Card desactivada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cliente/giftcards";
    }
    
    @GetMapping("/enviar")
    public String mostrarFormularioEnvio(Model model) {
        model.addAttribute("enviarGiftCardForm", new GiftCardDTO());
        return "cliente/enviar-giftcard";
    }
    
    @PostMapping("/enviar")
    public String enviarGiftCard(@Valid @ModelAttribute("enviarGiftCardForm") GiftCardDTO giftCardDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cliente/enviar-giftcard";
        }
        
        try {
            boolean enviado = giftCardServicio.enviarGiftCard(giftCardDTO);
            if (enviado) {
                redirectAttributes.addFlashAttribute("mensaje", "Gift Card enviada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo enviar la Gift Card");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cliente/giftcards";
    }
    
    // API REST endpoints para acceso desde JavaScript
    
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<GiftCardDTO>> listarGiftCardsApi() {
        List<GiftCardDTO> giftCards = giftCardServicio.obtenerGiftCardsCliente();
        return ResponseEntity.ok(giftCards);
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<GiftCardDTO> obtenerGiftCardApi(@PathVariable Long id) {
        GiftCardDTO giftCard = giftCardServicio.obtenerGiftCardPorId(id);
        return ResponseEntity.ok(giftCard);
    }
    
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> crearGiftCardApi(@Valid @RequestBody GiftCardDTO giftCardDTO) {
        try {
            GiftCardDTO giftCardCreada = giftCardServicio.crearGiftCard(giftCardDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(giftCardCreada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/activar")
    @ResponseBody
    public ResponseEntity<?> activarGiftCardApi(@PathVariable Long id) {
        try {
            GiftCardDTO giftCardActivada = giftCardServicio.activarGiftCard(id);
            return ResponseEntity.ok(giftCardActivada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/desactivar")
    @ResponseBody
    public ResponseEntity<?> desactivarGiftCardApi(@PathVariable Long id) {
        try {
            GiftCardDTO giftCardDesactivada = giftCardServicio.desactivarGiftCard(id);
            return ResponseEntity.ok(giftCardDesactivada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/verificar-saldo")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verificarSaldoApi(
            @RequestParam String codigo, 
            @RequestParam double monto) {
        
        boolean saldoSuficiente = giftCardServicio.verificarSaldo(codigo, monto);
        
        return ResponseEntity.ok(Map.of("saldoSuficiente", saldoSuficiente));
    }
}
package com.felicita.servicios.impl;

import com.felicita.dto.GiftCardDTO;
import com.felicita.entidades.Cliente;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.GiftCard;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.GiftCardRepositorio;
import com.felicita.servicios.ClienteServicio;
import com.felicita.servicios.GiftCardServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GiftCardServicioImpl implements GiftCardServicio {

    @Autowired
    private GiftCardRepositorio giftCardRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private ClienteServicio clienteServicio;

    @Override
    @Transactional
    public GiftCardDTO crearGiftCard(GiftCardDTO giftCardDTO) {
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        
        Establecimiento establecimiento = establecimientoRepositorio.findById(giftCardDTO.getEstablecimientoId())
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado"));
        
        // Crear nueva gift card
        GiftCard giftCard = new GiftCard();
        giftCard.setCliente(cliente);
        giftCard.setEstablecimiento(establecimiento);
        giftCard.setValor(giftCardDTO.getValor());
        giftCard.setSaldo(giftCardDTO.getValor());
        giftCard.setMensaje(giftCardDTO.getMensaje());
        giftCard.setFechaEmision(LocalDateTime.now());
        giftCard.setFechaExpiracion(LocalDate.now().plusYears(1));
        giftCard.setActiva(true);
        
        GiftCard giftCardGuardada = giftCardRepositorio.save(giftCard);
        
        return convertirADTO(giftCardGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public GiftCardDTO obtenerGiftCardPorId(Long id) {
        GiftCard giftCard = giftCardRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Gift Card no encontrada con ID: " + id));
        
        // Verificar que la gift card pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!giftCard.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new IllegalArgumentException("No tienes permiso para ver esta Gift Card");
        }
        
        return convertirADTO(giftCard);
    }

    @Override
    @Transactional(readOnly = true)
    public GiftCardDTO obtenerGiftCardPorCodigo(String codigo) {
        GiftCard giftCard = giftCardRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Gift Card no encontrada con código: " + codigo));
        
        return convertirADTO(giftCard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiftCardDTO> obtenerGiftCardsCliente() {
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        
        List<GiftCard> giftCards = giftCardRepositorio.findGiftCardsActivasByCliente(
                cliente.getId(), LocalDate.now());
        
        return giftCards.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GiftCardDTO activarGiftCard(Long id) {
        GiftCard giftCard = giftCardRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Gift Card no encontrada con ID: " + id));
        
        // Verificar que la gift card pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!giftCard.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new IllegalArgumentException("No tienes permiso para activar esta Gift Card");
        }
        
        // Verificar que no esté expirada
        if (giftCard.getFechaExpiracion().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede activar una Gift Card expirada");
        }
        
        giftCard.setActiva(true);
        GiftCard giftCardActualizada = giftCardRepositorio.save(giftCard);
        
        return convertirADTO(giftCardActualizada);
    }

    @Override
    @Transactional
    public GiftCardDTO desactivarGiftCard(Long id) {
        GiftCard giftCard = giftCardRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Gift Card no encontrada con ID: " + id));
        
        // Verificar que la gift card pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!giftCard.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new IllegalArgumentException("No tienes permiso para desactivar esta Gift Card");
        }
        
        giftCard.setActiva(false);
        GiftCard giftCardActualizada = giftCardRepositorio.save(giftCard);
        
        return convertirADTO(giftCardActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarSaldo(String codigo, double monto) {
        GiftCard giftCard = giftCardRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Gift Card no encontrada con código: " + codigo));
        
        // Verificar que la gift card esté activa y no expirada
        if (!giftCard.isActiva() || giftCard.getFechaExpiracion().isBefore(LocalDate.now())) {
            return false;
        }
        
        // Verificar saldo suficiente
        return giftCard.getSaldo().compareTo(BigDecimal.valueOf(monto)) >= 0;
    }

    @Override
    @Transactional
    public GiftCardDTO reducirSaldo(String codigo, double monto) {
        GiftCard giftCard = giftCardRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Gift Card no encontrada con código: " + codigo));
        
        // Verificar que la gift card esté activa y no expirada
        if (!giftCard.isActiva()) {
            throw new IllegalArgumentException("La Gift Card no está activa");
        }
        
        if (giftCard.getFechaExpiracion().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La Gift Card ha expirado");
        }
        
        // Verificar saldo suficiente
        if (giftCard.getSaldo().compareTo(BigDecimal.valueOf(monto)) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en la Gift Card");
        }
        
        // Reducir saldo
        giftCard.setSaldo(giftCard.getSaldo().subtract(BigDecimal.valueOf(monto)));
        
        // Si el saldo llega a cero, desactivar la gift card
        if (giftCard.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            giftCard.setActiva(false);
        }
        
        GiftCard giftCardActualizada = giftCardRepositorio.save(giftCard);
        
        return convertirADTO(giftCardActualizada);
    }

    @Override
    @Transactional
    public boolean enviarGiftCard(GiftCardDTO giftCardDTO) {
        // Aquí iría la lógica para enviar una gift card por email
        // Como mencionaste que no usarás servicios de correo, simplemente
        // registramos la gift card y retornamos true
        
        GiftCardDTO giftCardCreada = crearGiftCard(giftCardDTO);
        return giftCardCreada != null;
    }
    
    // Métodos privados de conversión
    private GiftCardDTO convertirADTO(GiftCard giftCard) {
        GiftCardDTO dto = new GiftCardDTO();
        dto.setId(giftCard.getId());
        dto.setCodigo(giftCard.getCodigo());
        dto.setClienteId(giftCard.getCliente().getId());
        dto.setEstablecimientoId(giftCard.getEstablecimiento().getId());
        dto.setNombreEstablecimiento(giftCard.getEstablecimiento().getNombre());
        dto.setValor(giftCard.getValor());
        dto.setSaldo(giftCard.getSaldo());
        dto.setFechaEmision(giftCard.getFechaEmision());
        dto.setFechaExpiracion(giftCard.getFechaExpiracion());
        dto.setMensaje(giftCard.getMensaje());
        dto.setActiva(giftCard.isActiva());
        dto.setValida(giftCard.esValida());
        
        return dto;
    }
}
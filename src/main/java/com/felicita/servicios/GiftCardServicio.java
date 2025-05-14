package com.felicita.servicios;

import com.felicita.dto.GiftCardDTO;
import java.util.List;

public interface GiftCardServicio {
    GiftCardDTO crearGiftCard(GiftCardDTO giftCardDTO);
    GiftCardDTO obtenerGiftCardPorId(Long id);
    GiftCardDTO obtenerGiftCardPorCodigo(String codigo);
    List<GiftCardDTO> obtenerGiftCardsCliente();
    GiftCardDTO activarGiftCard(Long id);
    GiftCardDTO desactivarGiftCard(Long id);
    boolean verificarSaldo(String codigo, double monto);
    GiftCardDTO reducirSaldo(String codigo, double monto);
    boolean enviarGiftCard(GiftCardDTO giftCardDTO);
}
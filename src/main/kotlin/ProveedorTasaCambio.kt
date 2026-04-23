package org.iesra.revilofe

/**
 * Interfaz para obtener la tasa de cambio de un par de monedas.
 */
interface ProveedorTasaCambio {
    /**
     * Devuelve la tasa para el par indicado, p.ej. "USDEUR".
     * @param par El par de monedas en formato "MONEDA_ORIGENMONEDA_DESTINO", p.ej. "USDEUR".
     * @return La tasa de cambio para el par de monedas.
     * @throws IllegalArgumentException Si no se encuentra la tasa para el par.
     * @throws IllegalArgumentException Si la tasa es negativa o cero.
     */
    fun tasa(par: String): Double
}
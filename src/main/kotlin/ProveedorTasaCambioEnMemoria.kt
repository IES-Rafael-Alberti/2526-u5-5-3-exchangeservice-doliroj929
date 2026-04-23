package org.iesra.revilofe

/**
 * Implementación en memoria de ProveedorTasaCambio.
 * Recibe un mapa de pares de moneda (por ejemplo "USDEUR") a tasa.
 */
class ProveedorTasaCambioEnMemoria(
    private val tasas: Map<String, Double>
) : ProveedorTasaCambio {
    init {
        require(tasas.all { (k, _) -> k.length == 6 }) { "Formato de par de monedas inválido" }
    }

    override fun tasa(par: String): Double {
        val parNormalizado = par.uppercase()
        val tasa = tasas[parNormalizado] ?: throw IllegalArgumentException("Tasa no encontrada para el par $parNormalizado")
        require(tasa > 0) { "La tasa debe ser positiva para el par $parNormalizado" }
        return tasa
    }
}

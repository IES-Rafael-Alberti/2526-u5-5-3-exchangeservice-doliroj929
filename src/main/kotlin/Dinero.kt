package org.iesra.revilofe

/**
 * Representa una cantidad de dinero en una moneda.
 * @constructor Crea una instancia de Dinero.
 * @property cantidad La cantidad de dinero, en la unidad más pequeña de la moneda (p.ej. centavos).
 * @property moneda El código de la moneda, en formato ISO 4217 (tres letras).
 *
 */
data class Dinero(val cantidad: Long, val moneda: String)
package org.iesra.revilofe

/**
 * Servicio que realiza conversiones de moneda usando un [ProveedorTasaCambio].
 * Soporta monedas con códigos de 3 letras (ISO 4217).
 * Soporta una conversión cruzada de un solo paso, es decir, A -> C -> B, pero no A -> C -> D -> B.
 * Requiere que las monedas de origen y destino sean válidas y que la cantidad sea positiva.
 * Si la moneda de origen y destino son iguales, devuelve la cantidad original sin consultar tasas.
 * Si la tasa directa existe, la usa; si no, intenta una conversión cruzada con monedas intermedias.
 *
 * @property proveedorTasa Proveedor de tasas de cambio que implementa ProveedorTasaCambio.
 * @param monedasSoportadas Conjunto de monedas soportadas para conversiones cruzadas.
 * @constructor Crea un servicio de intercambio de divisas.
 * @see ProveedorTasaCambio
 * @see Dinero
 *
 */
class ServicioCambio(
    private val proveedorTasa: ProveedorTasaCambio,
    private val monedasSoportadas: Set<String> = setOf("USD", "EUR", "GBP", "JPY")
) {
    /**
     * Convierte la cantidad `dinero` a la `monedaDestino`.
     * Si no existe la tasa directa, intenta una conversión cruzada.
     * Si la moneda de origen y destino son iguales, devuelve la cantidad original.
     * @param dinero La cantidad de dinero a convertir, con su moneda.
     * @param monedaDestino La moneda a la que se desea convertir, en formato ISO 4217 (tres letras).
     * @return La cantidad convertida a la moneda de destino, en la unidad más pequeña de esa moneda (p.ej. centavos).
     * @throws IllegalArgumentException Si la moneda de destino o la moneda del dinero no son válidas, o si la cantidad es negativa o cero.
     * @throws IllegalArgumentException Si no se encuentra una tasa para el par directo ni para ninguna conversión cruzada.
     *
     */
    fun cambiar(dinero: Dinero, monedaDestino: String): Long {
        // Verifica que la moneda de destino y la moneda del dinero sean válidas
        require(monedaDestino.length == 3) { "Código de moneda inválido: $monedaDestino" }
        require(dinero.moneda.length == 3) { "Código de moneda inválido: ${dinero.moneda}" }
        require(dinero.cantidad > 0) { "La cantidad debe ser positiva: ${dinero.cantidad}" }

        // Verifica si la moneda de origen y destino son iguales
        if (dinero.moneda.equals(monedaDestino, ignoreCase = true)) {
            return dinero.cantidad
        }

        // Verifica si la moneda de origen y destino son soportadas
        val parDirecto = "${dinero.moneda.uppercase()}${monedaDestino.uppercase()}"
        val tasaDirecta = runCatching { proveedorTasa.tasa(parDirecto) }.getOrNull()
        if (tasaDirecta != null && tasaDirecta > 0) {
            return (dinero.cantidad * tasaDirecta).toLong()
        }

        // Si no hay tasa directa, intenta una conversión cruzada.
        val intermedias = monedasSoportadas.filter {
            !it.equals(dinero.moneda, ignoreCase = true) &&
                    !it.equals(monedaDestino, ignoreCase = true)
        }

        var resultadoConversionCruzada: Long? = null
        var indice = 0

        while (indice < intermedias.size && resultadoConversionCruzada == null) {
            val intermedia = intermedias[indice]
            val par1 = "${dinero.moneda.uppercase()}${intermedia.uppercase()}"
            val par2 = "${intermedia.uppercase()}${monedaDestino.uppercase()}"

            val tasa1 = runCatching { proveedorTasa.tasa(par1) }.getOrNull()
            val tasa2 = runCatching { proveedorTasa.tasa(par2) }.getOrNull()

            if (tasa1 != null && tasa1 > 0 && tasa2 != null && tasa2 > 0) {
                resultadoConversionCruzada = (dinero.cantidad * tasa1 * tasa2).toLong()
            }

            indice++
        }

        // Si no se encontró una conversión directa ni cruzada, lanza una excepción
        return resultadoConversionCruzada ?: throw IllegalArgumentException(
            "No se encontró tasa para el par $parDirecto ni ninguna conversión cruzada."
        )
    }
}
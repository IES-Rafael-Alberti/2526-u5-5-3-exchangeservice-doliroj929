package com.example.exchange

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.iesra.revilofe.ProveedorTasaCambio
import org.iesra.revilofe.ServicioCambio
import org.iesra.revilofe.ProveedorTasaCambioEnMemoria
import org.iesra.revilofe.Dinero


class PruebaBateriaServicioCambio : DescribeSpec({

    // Definimos el Mock principal para el proveedor
    val proveedorMock = mockk<ProveedorTasaCambio>()
    // Inyectamos el mock en el servicio
    val servicio = ServicioCambio(proveedorMock)

    // Limpiamos los mocks después de cada test para evitar efectos colaterales
    afterTest {
        clearAllMocks()
    }

    describe("Batería de Pruebas para ServicioCambio") {

        // --- CLASE DE EQUIVALENCIA A: VALIDACIÓN DE ENTRADA ---
        describe("Validación de entradas (Clases inválidas)") {

            it("1. Debe lanzar excepción si la cantidad es 0") {
                val error = shouldThrow<IllegalArgumentException> {
                    servicio.cambiar(Dinero(0, "USD"), "EUR")
                }
                error.message shouldBe "La cantidad debe ser positiva: 0"
            }

            it("2. Debe lanzar excepción si la cantidad es negativa") {
                shouldThrow<IllegalArgumentException> {
                    servicio.cambiar(Dinero(-100, "USD"), "EUR")
                }
            }

            it("3. Debe lanzar excepción si la moneda origen no tiene 3 letras") {
                shouldThrow<IllegalArgumentException> {
                    servicio.cambiar(Dinero(100, "US"), "EUR")
                }
            }

            it("4. Debe lanzar excepción si la moneda destino no tiene 3 letras") {
                shouldThrow<IllegalArgumentException> {
                    servicio.cambiar(Dinero(100, "USD"), "EURO")
                }
            }
        }

        // --- CLASE DE EQUIVALENCIA B: RELACIÓN ORIGEN/DESTINO ---
        describe("Relación entre moneda origen y destino") {

            it("5. Debe devolver la misma cantidad si origen y destino son iguales") {
                // Justificación de SPY: Usamos un spy para verificar que el proveedor NO es consultado.
                val proveedorSpy = spyk<ProveedorTasaCambio>()
                val servicioConSpy = ServicioCambio(proveedorSpy)

                val resultado = servicioConSpy.cambiar(Dinero(500, "EUR"), "EUR")

                resultado shouldBe 500
                verify(exactly = 0) { proveedorSpy.tasa(any()) }
            }

            it("6. Debe convertir correctamente usando una tasa directa (Uso de STUB)") {
                // Justificación de STUB: Solo necesitamos una respuesta fija para validar el cálculo.
                every { proveedorMock.tasa("USDEUR") } returns 0.92

                val resultado = servicio.cambiar(Dinero(1000, "USD"), "EUR")

                resultado shouldBe 920
            }

            it("7. Debe usar SPY sobre el proveedor real para verificar una llamada correcta") {
                // Justificación de SPY: Envolvemos la implementación real para observar la interacción.
                val realProvider = ProveedorTasaCambioEnMemoria(mapOf("EURGBP" to 0.85))
                val spyProvider = spyk(realProvider)
                val servicioLocal = ServicioCambio(spyProvider)

                servicioLocal.cambiar(Dinero(100, "EUR"), "GBP") shouldBe 85

                // Verificamos que se llamó exactamente con el par esperado
                verify(exactly = 1) { spyProvider.tasa("EURGBP") }
            }
        }

        // --- CLASE DE EQUIVALENCIA C: ESTRATEGIA DE BÚSQUEDA ---
        describe("Estrategia de búsqueda de tasas (Conversión Cruzada)") {

            it("8. Debe resolver una conversión cruzada cuando la tasa directa no existe (Uso de MOCK)") {
                // Justificación de MOCK: Controlamos el flujo donde la primera falla y las siguientes funcionan.
                every { proveedorMock.tasa("GBPJPY") } throws IllegalArgumentException() // No hay directa
                every { proveedorMock.tasa("GBPUSD") } returns 1.25                      // Tramo 1
                every { proveedorMock.tasa("USDJPY") } returns 150.0                     // Tramo 2

                val resultado = servicio.cambiar(Dinero(10, "GBP"), "JPY")

                resultado shouldBe (10 * 1.25 * 150.0).toLong()
            }

            it("9. Debe intentar una segunda ruta intermedia si la primera falla (Uso de MOCK)") {
                every { proveedorMock.tasa("USDJPY") } throws IllegalArgumentException() // Sin directa

                // Ruta 1: USD -> EUR -> JPY (Falla)
                every { proveedorMock.tasa("USDEUR") } returns 0.9
                every { proveedorMock.tasa("EURJPY") } throws IllegalArgumentException()

                // Ruta 2: USD -> GBP -> JPY (Éxito)
                every { proveedorMock.tasa("USDGBP") } returns 0.8
                every { proveedorMock.tasa("GBPJPY") } returns 190.0

                servicio.cambiar(Dinero(10, "USD"), "JPY") shouldBe (10 * 0.8 * 190.0).toLong()
            }

            it("10. Debe lanzar excepción si no existe ninguna ruta válida") {
                // Simulamos que el proveedor no conoce ninguna tasa
                every { proveedorMock.tasa(any()) } throws IllegalArgumentException()

                shouldThrow<IllegalArgumentException> {
                    servicio.cambiar(Dinero(100, "USD"), "JPY")
                }
            }

            it("11. Debe verificar el orden exacto de las llamadas en una conversión cruzada") {
                every { proveedorMock.tasa("USDJPY") } throws IllegalArgumentException()
                every { proveedorMock.tasa("USDEUR") } returns 0.9
                every { proveedorMock.tasa("EURJPY") } returns 160.0

                servicio.cambiar(Dinero(10, "USD"), "JPY")

                // Verificamos que se intenta primero la directa y luego los tramos de la cruzada
                verifySequence {
                    proveedorMock.tasa("USDJPY")
                    proveedorMock.tasa("USDEUR")
                    proveedorMock.tasa("EURJPY")
                }
            }
        }
    }
})



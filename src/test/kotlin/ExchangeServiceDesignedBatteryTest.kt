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


    }
})



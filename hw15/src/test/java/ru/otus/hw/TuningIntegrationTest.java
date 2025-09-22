package ru.otus.hw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.gateway.TuningGateway;
import ru.otus.hw.model.BigTireSUV;
import ru.otus.hw.model.SUV;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.shell.interactive.enabled=false",
        "spring.shell.script.enabled=false"
})
class TuningIntegrationTest {

    @Autowired
    private TuningGateway tuningGateway;

    @Test
    void testFullTuningProcess() {
        // Given
        SUV inputSUV = SUV.builder()
                .brand("Toyota")
                .model("Land Cruiser")
                .build();

        // When
        BigTireSUV tunedSUV = tuningGateway.tuneSUV(inputSUV);

        // Then
        assertNotNull(tunedSUV);
        assertEquals("Toyota", tunedSUV.getBrand());
        assertEquals("Land Cruiser", tunedSUV.getModel());

        assertTrue(tunedSUV.isSnorkelInstalled());
        assertTrue(tunedSUV.isWinchInstalled());
        assertTrue(tunedSUV.isProtectionInstalled());
        assertTrue(tunedSUV.isSuspensionUpgraded());
        assertTrue(tunedSUV.isBigTiresInstalled());
        assertEquals(2.5, tunedSUV.getLiftHeightInches());
        assertEquals("35x12.5R17", tunedSUV.getTireSize());
    }
}
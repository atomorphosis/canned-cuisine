package atomorphosis.cannedcuisine.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ReserveHealthTest {
    @Test
    void validatesStoredReservePoints() {
        assertEquals(0.0F, ReserveHealthValue.ZERO.points());
        assertEquals(20.0F, new ReserveHealthValue(20.0F).points());
        assertThrows(IllegalArgumentException.class, () -> new ReserveHealthValue(-1.0F));
        assertThrows(IllegalArgumentException.class, () -> new ReserveHealthValue(21.0F));
        assertThrows(IllegalArgumentException.class, () -> new ReserveHealthValue(Float.NaN));
    }

    @Test
    void consumesVanillaAbsorptionBeforeReserve() {
        assertResolution(3.0F, 4.0F, 6.0F, 3.0F, 6.0F);
        assertResolution(7.0F, 4.0F, 6.0F, 4.0F, 3.0F);
        assertResolution(12.0F, 4.0F, 6.0F, 6.0F, 0.0F);
        assertResolution(4.0F, 0.0F, 6.0F, 0.0F, 2.0F);
        assertResolution(7.0F, 4.0F, 0.0F, 7.0F, 0.0F);
    }

    private static void assertResolution(
            float damage,
            float absorption,
            float reserve,
            float expectedDamage,
            float expectedReserve
    ) {
        var resolution = ReserveHealth.resolveDamage(damage, absorption, reserve);
        assertEquals(expectedDamage, resolution.remainingDamage());
        assertEquals(expectedReserve, resolution.remainingReserve());
    }
}

package atomorphosis.cannedcuisine.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TemporaryHealthPayloadTest {
    @Test
    void acceptsOnlyFiniteHealthWithinTheResolvedCap() {
        assertEquals(0.0F, new TemporaryHealthPayload(0.0F).amount());
        assertEquals(20.0F, new TemporaryHealthPayload(20.0F).amount());
        assertThrows(IllegalArgumentException.class, () -> new TemporaryHealthPayload(-1.0F));
        assertThrows(IllegalArgumentException.class, () -> new TemporaryHealthPayload(21.0F));
        assertThrows(IllegalArgumentException.class, () -> new TemporaryHealthPayload(Float.NaN));
    }
}

package atomorphosis.cannedcuisine.engine.effect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EffectDistributorTest {
    @Test
    void givesEveryProducedCanTheFullServingDuration() {
        var effect = new ResolvedEffect(
                new EffectId("minecraft", "speed"),
                0.75,
                0,
                100
        );

        var perCan = EffectDistributor.perCan(new EffectSelection(List.of(effect)), 3);

        assertEquals(100, perCan.getFirst().durationTicks());
    }

    @Test
    void returnsAnImmutablePerCanEffectList() {
        var perCan = EffectDistributor.perCan(new EffectSelection(List.of(
                new ResolvedEffect(new EffectId("minecraft", "speed"), 0.75, 0, 1200)
        )), 2);

        assertThrows(UnsupportedOperationException.class, perCan::clear);
    }
}

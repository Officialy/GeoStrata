package reika.geostrata.level.generators;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import reika.geostrata.base.VentType;

import static org.junit.jupiter.api.Assertions.*;

class VentGeneratorTest {

    @Test
    void zeroWeightHeightsProduceNoVent() {
        RandomSource unused = fixedRoll(() -> {
            fail("An empty distribution must not consume the placement RNG");
            return 0;
        });
        for (int height : new int[]{0, 72, 128, 256})
            assertNull(VentGenerator.selectVentType(height, false, true, unused));
        for (int height : new int[]{0, 128, 256})
            assertNull(VentGenerator.selectVentType(height, true, false, unused));
    }

    @Test
    void everySelectedVentHasPositiveWeightAndMatchesDimensionAndBiome() {
        RandomSource random = RandomSource.create(6329);
        for (boolean nether : new boolean[]{false, true}) {
            for (boolean snow : new boolean[]{false, true}) {
                int limit = nether ? 128 : 72;
                for (int height = 1; height < limit; height++) {
                    for (int roll = 0; roll < 100; roll++) {
                        VentType type = VentGenerator.selectVentType(height, nether, snow, random);
                        assertNotNull(type, "No vent at eligible height " + height);
                        assertTrue(type.getSpawnWeight(height, nether) > 0);
                        assertTrue(nether ? type.canGenerateInNether() : type.canGenerateInOverworld());
                        assertTrue(type != VentType.CRYO || snow);
                    }
                }
            }
        }
    }

    @Test
    void zeroRandomRollCannotSelectAZeroWeightVent() {
        RandomSource zero = fixedRoll(() -> 0);
        for (int height : new int[]{24, 32, 40, 60, 71}) {
            VentType type = VentGenerator.selectVentType(height, false, false, zero);
            assertNotNull(type);
            assertTrue(type.getSpawnWeight(height, false) > 0);
            assertNotEquals(VentType.CRYO, type);
        }
    }

    @Test
    void anotherPlacementCannotChangeTheWeightSnapshotMidRoll() {
        VentType expected = VentGenerator.selectVentType(24, false, false, fixedRoll(() -> 0.99));
        RandomSource interleaved = fixedRoll(() -> {
            assertNull(VentGenerator.selectVentType(0, false, false, RandomSource.create(1)));
            assertNotNull(VentGenerator.selectVentType(100, true, false, RandomSource.create(2)));
            return 0.99;
        });
        assertEquals(expected, VentGenerator.selectVentType(24, false, false, interleaved));
    }

    @Test
    void concurrentPlacementSelectionsMatchSerialSeededResults() throws Exception {
        int workers = 8;
        List<List<VentType>> expected = new ArrayList<>();
        List<Callable<List<VentType>>> tasks = new ArrayList<>();
        CyclicBarrier barrier = new CyclicBarrier(workers);
        for (int worker = 0; worker < workers; worker++) {
            int seed = worker;
            expected.add(sequence(seed));
            tasks.add(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return sequence(seed);
            });
        }
        try (var executor = Executors.newFixedThreadPool(workers)) {
            var results = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
            for (int worker = 0; worker < workers; worker++)
                assertEquals(expected.get(worker), results.get(worker).get());
        }
    }

    private static List<VentType> sequence(int seed) {
        RandomSource random = RandomSource.create(seed);
        List<VentType> result = new ArrayList<>();
        for (int roll = 0; roll < 4096; roll++) {
            boolean nether = (roll + seed) % 2 == 0;
            int height = 1 + (roll * 17 + seed) % (nether ? 127 : 71);
            VentType type = VentGenerator.selectVentType(height, nether, roll % 3 == 0, random);
            assertNotNull(type);
            result.add(type);
        }
        return result;
    }

    private static RandomSource fixedRoll(DoubleSupplier roll) {
        return (RandomSource) Proxy.newProxyInstance(RandomSource.class.getClassLoader(),
                new Class<?>[]{RandomSource.class}, (proxy, method, args) -> {
                    if (method.getName().equals("nextDouble"))
                        return roll.getAsDouble();
                    throw new AssertionError("Unexpected RNG call: " + method.getName());
                });
    }
}

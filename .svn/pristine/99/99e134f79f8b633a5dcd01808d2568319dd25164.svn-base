import org.junit.Before;
import org.junit.Test;
import project.ResultsControllerBean;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.*;

public class HitDetectionEdgeCasesTest {

    private ResultsControllerBean controller;

    @Before
    public void setUp() {
        controller = new ResultsControllerBean();
    }

    @Test
    public void testLeftTriangleBoundary() {
        assertTrue(controller.calculateHitResult(0.0, -2.0, 2.0));
        assertTrue(controller.calculateHitResult(-2.0, 0.0, 2.0));
        assertFalse(controller.calculateHitResult(-1.5, -0.5, 1.0));
    }

    @Test
    public void testRectangleBoundary() {
        assertTrue(controller.calculateHitResult(2.0, -1.0, 2.0));
        assertTrue(controller.calculateHitResult(0.0, 0.0, 2.0));
        assertFalse(controller.calculateHitResult(2.1, -1.0, 2.0));
    }

    @Test
    public void testCircleBoundary() {
        double r = Math.sqrt(8.0);
        assertTrue(controller.calculateHitResult(-1.0, 1.0, r));
        assertFalse(controller.calculateHitResult(-2.0, 2.0, 2.0));
    }
}

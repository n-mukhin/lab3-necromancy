import org.junit.Before;
import org.junit.Test;
import project.ResultsControllerBean;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ResultsControllerBeanTest {

    private ResultsControllerBean controller;

    @Before
    public void setUp() {
        controller = new ResultsControllerBean();
    }

    @Test
    public void testCalculateHitResult_PointInsideRectangle() {
        assertTrue(controller.calculateHitResult(1.0, -1.0, 2.0));
    }

    @Test
    public void testCalculateHitResult_PointInsideCircle() {
        assertTrue(controller.calculateHitResult(-1.0, 1.0, 4.0));
    }

    @Test
    public void testCalculateHitResult_PointInsideTriangle() {
        assertTrue(controller.calculateHitResult(-1.0, -1.0, 2.0));
    }

    @Test
    public void testCalculateHitResult_PointOutsideAll() {
        assertFalse(controller.calculateHitResult(3.0, 3.0, 1.0));
    }

    @Test
    public void testCalculateExecutionTime() {
        long start = 100_000_000L;
        long end = 150_000_000L;
        BigDecimal result = controller.calculateExecutionTime(start, end);
        assertEquals(new BigDecimal("50.000"), result);
    }

    @Test
    public void testInitialResultCount() {
        assertEquals(0, controller.getResultCount());
    }
}

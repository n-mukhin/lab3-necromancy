import org.junit.Test;
import project.ResultsControllerBean;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.*;

public class ExecutionTimeCalculationTest {

    private final ResultsControllerBean controller = new ResultsControllerBean();

    @Test
    public void testZeroDuration() {
        long start = 100_000_000L;
        long end = 100_000_000L;
        BigDecimal result = controller.calculateExecutionTime(start, end);
        assertEquals(BigDecimal.ZERO.setScale(3), result);
    }

    @Test
    public void testSmallDuration() {
        long start = 0L;
        long end = 1_000_000L;
        BigDecimal result = controller.calculateExecutionTime(start, end);
        assertEquals(new BigDecimal("1.000"), result);
    }

    @Test
    public void testLargeDuration() {
        long start = 0L;
        long end = 123_456_789L;
        BigDecimal result = controller.calculateExecutionTime(start, end);
        BigDecimal expected = BigDecimal.valueOf(123_456_789L)
                .divide(BigDecimal.valueOf(1_000_000), 3, RoundingMode.HALF_UP);
        assertEquals(expected, result);
    }
}

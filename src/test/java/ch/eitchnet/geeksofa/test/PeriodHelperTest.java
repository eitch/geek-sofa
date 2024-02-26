package ch.eitchnet.geeksofa.test;

import ch.eitchnet.geeksofa.model.VideosHelper;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class PeriodHelperTest {

	@Test
	public void testEndOfYear() {
		LocalDate d1 = LocalDate.of(2024, 1, 1);
		LocalDate d2 = LocalDate.of(2011, 12, 31);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, false);
		Assert.assertEquals(1, days);
	}

	@Test
	public void testEndOfYearReverse() {
		LocalDate d1 = LocalDate.of(2011, 12, 31);
		LocalDate d2 = LocalDate.of(2024, 1, 1);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, false);
		Assert.assertEquals(1, days);
	}

	@Test
	public void testEndOfYearReverseSign() {
		LocalDate d1 = LocalDate.of(2011, 12, 31);
		LocalDate d2 = LocalDate.of(2024, 1, 1);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, true);
		Assert.assertEquals(1, days);
	}

	@Test
	public void testInYear() {
		LocalDate d1 = LocalDate.of(2024, 1, 1);
		LocalDate d2 = LocalDate.of(2024, 1, 2);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, false);
		Assert.assertEquals(1, days);
	}

	@Test
	public void testInYearSigned() {
		LocalDate d1 = LocalDate.of(2024, 1, 1);
		LocalDate d2 = LocalDate.of(2024, 1, 2);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, true);
		Assert.assertEquals(1, days);
	}

	@Test
	public void testInYearReverse() {
		LocalDate d1 = LocalDate.of(2024, 1, 2);
		LocalDate d2 = LocalDate.of(2024, 1, 1);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, false);
		Assert.assertEquals(1, days);
	}

	@Test
	public void testInYearReverseSigned() {
		LocalDate d1 = LocalDate.of(2024, 1, 2);
		LocalDate d2 = LocalDate.of(2024, 1, 1);
		int days = VideosHelper.daysBetweenIgnoreYear(d1, d2, true);
		Assert.assertEquals(-1, days);
	}
}

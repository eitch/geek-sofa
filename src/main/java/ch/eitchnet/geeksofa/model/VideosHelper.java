package ch.eitchnet.geeksofa.model;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static ch.eitchnet.geeksofa.model.Constants.PARAM_START_TIME;
import static ch.eitchnet.geeksofa.model.Constants.TYPE_VIDEO;
import static li.strolch.utils.time.PeriodHelper.daysIn;

public class VideosHelper {

	public static List<Resource> findClosestVideo(Stream<Resource> videosStream, LocalDate date) {
		List<Resource> closestVideos = new ArrayList<>();
		AtomicReference<Integer> closesDaysBetween = new AtomicReference<>();
		videosStream.forEach(video -> {
			ZonedDateTime startTime = video.getDate(PARAM_START_TIME);
			int daysBetween = daysBetweenIgnoreYear(date, startTime.toLocalDate(), false);
			if (closesDaysBetween.get() == null) {
				closestVideos.add(video);
				closesDaysBetween.set(daysBetween);
			} else if (daysBetween < closesDaysBetween.get()) {
				closestVideos.clear();
				closestVideos.add(video);
				closesDaysBetween.set(daysBetween);
			} else if (daysBetween == closesDaysBetween.get()) {
				closestVideos.add(video);
			}
		});

		return closestVideos;
	}

	public static int daysBetweenIgnoreYear(LocalDate d1, LocalDate d2, boolean keepSign) {
		double days = daysIn(Period.between(d1, d2));
		int sign = (int) Math.signum(days);
		int daysWithYears = (int) Math.abs(days);
		int daysWithOutYear = (daysWithYears) - (365 * (daysWithYears / 365));
		if (keepSign)
			return daysWithOutYear * sign;
		return daysWithOutYear;
	}
}

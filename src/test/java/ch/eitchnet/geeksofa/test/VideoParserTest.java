package ch.eitchnet.geeksofa.test;

import ch.eitchnet.geeksofa.parser.VideoParser;
import li.strolch.model.Resource;
import li.strolch.utils.iso8601.ISO8601;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static ch.eitchnet.geeksofa.model.VideosHelper.daysBetweenIgnoreYear;
import static ch.eitchnet.geeksofa.model.VideosHelper.findClosestVideo;
import static org.junit.Assert.assertEquals;

public class VideoParserTest {

	private static final Logger logger = LoggerFactory.getLogger(VideoParserTest.class);

	@Test
	public void shouldParseVideos() {
		File csvFile = new File("runtime/data/srf_geeksofa_videos.csv");
		List<Resource> videos = new VideoParser(csvFile).parseVideos();
		assertEquals(991, videos.size());

		LocalDate today = LocalDate.of(2024, 1, 1);
		while (today.isBefore(LocalDate.of(2025, 1, 1))) {
			List<Resource> closestVideos = findClosestVideo(videos.stream(), today);

			if (closestVideos.isEmpty())
				continue;

			if (closestVideos.size() > 1) {
				logger.info("FOUND multiple closest for " + today + ":");
				for (Resource closestVideo : closestVideos) {
					LocalDate startTime = closestVideo.getDate(PARAM_START_TIME).toLocalDate();
					logger.info(MessageFormat.format("{0} {1} {2} / {3}", closestVideo.getString(PARAM_YT_ID),
							closestVideo.getString(PARAM_TITLE), startTime, today));
				}
				logger.info("");
			}

			Resource video = closestVideos.getFirst();
			LocalDate startTime = video.getDate(PARAM_START_TIME).toLocalDate();
			int daysBetween = daysBetweenIgnoreYear(today, startTime, true);
			if (daysBetween == 0) {
				today = today.plusDays(1);
				continue;
			}

			if (daysBetween < 0) {
				logger.info(MessageFormat.format("{0} {1} {2} / {3}: {4}", video.getString(PARAM_YT_ID),
						video.getString(PARAM_TITLE), startTime, today, daysBetween));
			} else {
				logger.info(MessageFormat.format("FUTURE: {0} {1} {2} / {3}: {4}", video.getString(PARAM_YT_ID),
						video.getString(PARAM_TITLE), startTime, today, daysBetween));
			}
			today = today.plusDays(1);
		}
	}
}

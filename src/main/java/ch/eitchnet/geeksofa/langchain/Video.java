package ch.eitchnet.geeksofa.langchain;

import li.strolch.agent.api.StrolchAgent;
import li.strolch.model.Resource;
import org.apache.commons.csv.CSVRecord;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static ch.eitchnet.geeksofa.model.VideosHelper.htmlify;

public record Video(String videoId, String title, String description, String url, ZonedDateTime startTime) {

	public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

	public Resource toResource() {
		Resource videoRes = new Resource(StrolchAgent.getUniqueId(), title, TYPE_VIDEO);
		videoRes.setString(PARAM_YT_ID, videoId);
		videoRes.setString(PARAM_TITLE, title);
		videoRes.setString(PARAM_URL, url);
		videoRes.setText(PARAM_DESCRIPTION, htmlify(description));
		videoRes.setDate(PARAM_START_TIME, startTime);
		return videoRes;
	}

	public static Video fromCsv(CSVRecord record) {
		String videoId = record.get(0);
		String title = record.get(1);
		String description = record.get(2);
		String startTime = record.get(3);

		String url = "https://www.youtube.com/watch?v=" + videoId;
		return new Video(videoId, title, description, url,
				LocalDateTime.parse(startTime, DT_FORMATTER).atZone(ZoneId.systemDefault()));
	}
}

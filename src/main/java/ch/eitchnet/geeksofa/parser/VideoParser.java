package ch.eitchnet.geeksofa.parser;

import li.strolch.agent.api.StrolchAgent;
import li.strolch.model.Resource;
import li.strolch.utils.dbc.DBC;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class VideoParser {

	private static final Logger logger = LoggerFactory.getLogger(VideoParser.class);

	public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	private final File csvFile;

	public VideoParser(File csvFile) {
		DBC.PRE.assertExists("CSV File must exist!", csvFile);
		this.csvFile = csvFile;
	}

	public List<Resource> parseVideos() {
		try (FileReader reader = new FileReader(csvFile, ISO_8859_1); CSVParser parser = new CSVParser(reader,
				CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setDelimiter(';').build())) {

			List<Resource> result = new ArrayList<>();
			for (CSVRecord record : parser) {
				try {
					result.add(parseRecord(record));
				} catch (Exception e) {
					throw new IllegalStateException(
							"Failed to parse line " + parser.getCurrentLineNumber() + ":\n" + record.toString(), e);
				}
			}

			logger.info("Parsed " + result.size() + " videos.");
			return result;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to parse CSV file " + this.csvFile.getAbsolutePath(), e);
		}
	}

	private Resource parseRecord(CSVRecord record) {
		String videoId = record.get(0);
		String title = record.get(1);
		String description = htmlify(record.get(2));
		String startTime = record.get(3);

		Resource videoRes = new Resource(StrolchAgent.getUniqueId(), title, TYPE_VIDEO);
		videoRes.setString(PARAM_YT_ID, videoId);
		videoRes.setString(PARAM_TITLE, title);
		videoRes.setText(PARAM_DESCRIPTION, description);
		videoRes.setDate(PARAM_START_TIME, LocalDateTime.parse(startTime, DT_FORMATTER).atZone(ZoneId.systemDefault()));

		return videoRes;
	}

	private String htmlify(String text) {
		String[] paragraphs = text.trim().split("\n\n");

		StringBuilder sb = new StringBuilder();

		for (String paragraph : paragraphs) {

			String[] lines = paragraph.split("\n");
			if (lines.length == 1) {
				appendParagraph(paragraph, sb);
			} else if (lines[0].startsWith("***") && lines[1].startsWith("(")) {
				appendListWithTitle(sb, lines, "(");
			} else if (!lines[0].startsWith("(") && lines[1].startsWith("(")) {
				appendListWithTitle(sb, lines, "(");
			} else if (!lines[0].startsWith("_") && lines[1].startsWith("_")) {
				appendListWithTitle(sb, lines, "_");
			} else if (lines[0].startsWith("(")) {
				appendList(sb, lines);
			} else {
				appendParagraphWithLineBreaks(sb, lines);
			}
		}

		return sb.toString();
	}

	private static void appendParagraph(String paragraph, StringBuilder sb) {
		sb.append("<p>").append(paragraph).append("</p>");
	}

	private void appendListWithTitle(StringBuilder sb, String[] lines, String itemChar) {
		sb.append("<h3>").append(lines[0]).append("</h3>");
		sb.append("<ul>");
		int i = 1;
		for (; i < lines.length; i++) {
			String line = lines[i];
			if (!line.startsWith(itemChar))
				break;
			sb.append("<li>").append(line).append("</li>");
		}
		sb.append("</ul>");
		for (; i < lines.length; i++) {
			sb.append(lines[i]).append("<br/>");
		}
	}

	private static void appendList(StringBuilder sb, String[] lines) {
		sb.append("<ul>");
		for (String line : lines) {
			sb.append("<li>").append(line).append("</li>");
		}
		sb.append("</ul>");
	}

	private static void appendParagraphWithLineBreaks(StringBuilder sb, String[] lines) {
		sb.append("<p>");
		for (String line : lines) {
			sb.append(line).append("<br/>");
		}
		sb.append("</p>");
	}
}

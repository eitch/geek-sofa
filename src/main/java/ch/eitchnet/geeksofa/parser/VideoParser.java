package ch.eitchnet.geeksofa.parser;

import ch.eitchnet.geeksofa.langchain.Video;
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
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class VideoParser {

	private static final Logger logger = LoggerFactory.getLogger(VideoParser.class);

	private final File csvFile;

	public VideoParser(File csvFile) {
		DBC.PRE.assertExists("CSV File must exist!", csvFile);
		this.csvFile = csvFile;
	}

	public Map<Video, Resource> parseVideos() {
		try (FileReader reader = new FileReader(csvFile, ISO_8859_1); CSVParser parser = new CSVParser(reader,
				CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setDelimiter(';').build())) {

			Map<Video, Resource> result = new HashMap<>();
			for (CSVRecord record : parser) {
				try {
					Video video = Video.fromCsv(record);
					result.put(video, video.toResource());
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
}

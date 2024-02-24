package ch.eitchnet.geeksofa.test;

import ch.eitchnet.geeksofa.model.VideoParser;
import li.strolch.model.Resource;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VideoParserTest {

	@Test
	public void shouldParseVideos() {
		File csvFile = new File("runtime/data/srf_geeksofa_videos.csv");
		List<Resource> videos = new VideoParser(csvFile).parseVideos();
		assertEquals(993, videos.size());
	}
}

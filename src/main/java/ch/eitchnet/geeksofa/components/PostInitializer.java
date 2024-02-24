package ch.eitchnet.geeksofa.components;

import ch.eitchnet.geeksofa.model.VideoParser;
import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.impl.SimplePostInitializer;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;

import java.io.File;
import java.util.List;

public class PostInitializer extends SimplePostInitializer {

	public PostInitializer(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	@Override
	public void start() throws Exception {
		File csvFile = getConfiguration()
				.getRuntimeConfiguration()
				.getDataFile(getName(), "srf_geeksofa_videos.csv", true);

		runAsAgent(ctx -> {
			try (StrolchTransaction tx = openTx(ctx.certificate(), false)) {
				List<Resource> videos = new VideoParser(csvFile).parseVideos();
				videos.forEach(tx::add);
				tx.commitOnClose();
			}
		});

		super.start();
	}
}

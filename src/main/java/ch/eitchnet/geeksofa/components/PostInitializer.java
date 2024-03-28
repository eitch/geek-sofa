package ch.eitchnet.geeksofa.components;

import ch.eitchnet.geeksofa.chatbot.ChatBotHandler;
import ch.eitchnet.geeksofa.chatbot.Video;
import ch.eitchnet.geeksofa.parser.VideoParser;
import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.impl.SimplePostInitializer;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.runtime.configuration.ComponentConfiguration;

import java.io.File;
import java.util.Map;

public class PostInitializer extends SimplePostInitializer {

	private String csvFile;

	public PostInitializer(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	@Override
	public void initialize(ComponentConfiguration configuration) throws Exception {
		this.csvFile = configuration.getString("csvFile", null);
		super.initialize(configuration);
	}

	@Override
	public void start() throws Exception {
		File csvFile = new File(this.csvFile);
		if (!csvFile.exists()) {
			logger.warn("CSV File {} is not absolute, trying from data path...", csvFile.getAbsolutePath());
			csvFile = getConfiguration().getRuntimeConfiguration().getDataFile(getName(), this.csvFile, true);
		}

		Map<Video, Resource> videos = new VideoParser(csvFile).parseVideos();

		runAsAgent(ctx -> {
			try (StrolchTransaction tx = openTx(ctx.certificate(), false)) {
				videos.values().forEach(tx::add);
				tx.commitOnClose();
			}
		});

		getComponent(ChatBotHandler.class).setVideos(videos.keySet());

		super.start();
	}
}

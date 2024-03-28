package ch.eitchnet.geeksofa.chatbot;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.api.StrolchComponent;
import li.strolch.runtime.configuration.ComponentConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static ch.eitchnet.geeksofa.chatbot.DocSearchService.*;

public class ChatBotHandler extends StrolchComponent {

	private AllMiniLmL6V2EmbeddingModel embeddingModel;
	private InMemoryEmbeddingStore<TextSegment> embeddingStore;
	private OpenAiStreamingChatModel chatModel;

	private List<ChatBotListener> listeners;
	private Map<String, ChatBotQuestion> questions;
	private Future<?> prepareModelTask;
	private String apiKey;

	public ChatBotHandler(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	@Override
	public void initialize(ComponentConfiguration configuration) throws Exception {
		if (configuration.hasProperty("apiKey")) {
			this.apiKey = configuration.getSecret("apiKey");
		} else {
			this.apiKey = ApiKeys.OPENAI_API_KEY;
			if (this.apiKey == null || this.apiKey.isEmpty())
				throw new IllegalStateException("Missing apiKey as configuration or environment variable!");
		}
		this.listeners = Collections.synchronizedList(new ArrayList<>());
		this.questions = new ConcurrentHashMap<>();
		addListener(this::logMessage);
		super.initialize(configuration);
	}

	private void logMessage(String message) {
		logger.info(message.trim());
	}

	public void addListener(ChatBotListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(ChatBotListener listener) {
		this.listeners.remove(listener);
	}

	public Optional<ChatBotQuestion> getQuestion(String questionId) {
		return Optional.ofNullable(questions.get(questionId));
	}

	public void setVideos(Collection<Video> videos) {
		this.prepareModelTask = getExecutorService(getName()).submit(() -> prepareModel(videos));
	}

	public boolean isNotReady() {
		return !isReady();
	}

	public boolean isReady() {
		return this.prepareModelTask != null && this.prepareModelTask.isDone();
	}

	private void prepareModel(Collection<Video> videos) {
		notifyListeners("\nInitiating for " + videos.size() + " videos...");

		notifyListeners("\nBuilding text segments...");
		List<TextSegment> textSegments = new ArrayList<>();
		videos.stream().filter(c -> !c.description().isEmpty()).forEach(contentSection -> {
			Map<String, String> metadataMap = new HashMap<>();
			metadataMap.put(OBJECT_ID, contentSection.videoId());
			metadataMap.put(URL, contentSection.url());
			String text = contentSection.title()
					+ "\n\n"
					+ "Geschrieben am "
					+ contentSection.startTime().toLocalDate()
					+ "\n\n"
					+ contentSection.description();
			textSegments.add(TextSegment.from(text, Metadata.from(metadataMap)));
		});
		notifyListeners("\nConverted to number of text segments: " + textSegments.size());

		this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
		notifyListeners("\nEmbedding model is created: " + this.embeddingModel);
		this.embeddingStore = new InMemoryEmbeddingStore<>();
		notifyListeners("\nEmbedding store is created: " + this.embeddingStore);

		notifyListeners("\nEmbedding segments...");
		List<Embedding> embeddings = this.embeddingModel.embedAll(textSegments).content();
		notifyListeners("\nNumber of embeddings: " + embeddings.size());

		this.embeddingStore.addAll(embeddings, textSegments);
		notifyListeners("\nEmbeddings are added to the store");

		this.chatModel = OpenAiStreamingChatModel.builder().apiKey(apiKey)
				// Available OpenAI models are listed on
				// https://platform.openai.com/docs/models/continuous-model-upgrades
				// gpt-4-1106-preview --> more expensive to use
				// gpt-4
				// gpt-3.5-turbo-1106
				.modelName(MODEL_NAME).build();
		notifyListeners("Chat model is ready");
	}

	private void notifyListeners(String message) {
		var listeners = this.listeners;
		synchronized (listeners) {
			listeners.forEach(l -> l.appendMessage(message));
		}
	}

	public ChatBotQuestion ask(String questionString) {
		if (isNotReady())
			throw new IllegalStateException("Model preparation is not yet done");
		ChatBotQuestion question = new ChatBotQuestion(this::notifyListeners, questionString);
		DocSearchService service = new DocSearchService(this.embeddingStore, this.embeddingModel, this.chatModel);
		service.ask(question);
		this.questions.put(question.getId(), question);
		return question;
	}
}

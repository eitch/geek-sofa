package ch.eitchnet.geeksofa.langchain;

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

import static ch.eitchnet.geeksofa.langchain.DocSearchService.*;

public class LangChainHandler extends StrolchComponent {

	private AllMiniLmL6V2EmbeddingModel embeddingModel;
	private InMemoryEmbeddingStore<TextSegment> embeddingStore;
	private OpenAiStreamingChatModel chatModel;

	private List<LangChainListener> listeners;

	public LangChainHandler(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	@Override
	public void initialize(ComponentConfiguration configuration) throws Exception {
		this.listeners = Collections.synchronizedList(new ArrayList<>());
		addListener(this::logMessage);
		super.initialize(configuration);
	}

	private void logMessage(String message) {
		logger.info(message.trim());
	}

	public void addListener(LangChainListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(LangChainListener listener) {
		this.listeners.remove(listener);
	}

	public void prepareModel(Collection<Video> videos) {
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

		this.chatModel = OpenAiStreamingChatModel.builder().apiKey(ApiKeys.OPENAI_API_KEY)
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

	public LangChainQuestion ask(String questionString) {
		LangChainQuestion question = new LangChainQuestion(this::notifyListeners, questionString);
		DocSearchService service = new DocSearchService(this.embeddingStore, this.embeddingModel, this.chatModel);
		service.ask(question);
		return question;
	}
}

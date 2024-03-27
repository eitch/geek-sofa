package ch.eitchnet.geeksofa.langchain;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocSearchService {

	public static final String MODEL_NAME = "gpt-3.5-turbo-1106";
	public static final String OBJECT_ID = "OBJECT_ID";
	public static final String URL = "URL";

	private static final Logger logger = LoggerFactory.getLogger(DocSearchService.class);

	private final AllMiniLmL6V2EmbeddingModel embeddingModel;
	private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
	private final OpenAiStreamingChatModel chatModel;

	public DocSearchService(InMemoryEmbeddingStore<TextSegment> embeddingStore,
			AllMiniLmL6V2EmbeddingModel embeddingModel, OpenAiStreamingChatModel chatModel) {
		this.embeddingModel = embeddingModel;
		this.embeddingStore = embeddingStore;
		this.chatModel = chatModel;
	}

	void ask(LangChainQuestion question) {
		logger.info("Asking question '{}'", question.getQuestion());

		// Find relevant embeddings in embedding store by semantic similarity
		// You can play with parameters below to find a sweet spot for your specific use case
		int maxResults = 10;
		double minScore = 0.7;
		List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore.findRelevant(
				embeddingModel.embed(question.getQuestion()).content(), maxResults, minScore);
		logger.info("Number of relevant embeddings: {} for '{}'", relevantEmbeddings.size(), question.getQuestion());

		relevantEmbeddings.stream().map(EmbeddingMatch::embedded).toList().forEach(ts -> {
			logger.info("Adding link: {}", ts.metadata(URL));
			question.addRelatedLink(ts.metadata(URL));
		});

		// Create a prompt for the model that includes question and relevant embeddings
		PromptTemplate promptTemplate = PromptTemplate.from(relevantEmbeddings.isEmpty() ? """
				Der Benutzer hat die folgende Frage gestellt:
					{{question}}
								
				Leider scheinen keine Videos einen Inhalt zu haben, der mit dieser Frage zusammenhängt.
				Bitte antworten Sie höflich, dass sie nach etwas anderem fragen sollen.
				""" : """
				Beantworten Sie die folgende Frage nach bestem Wissen und Gewissen:
					{{question}}
								
				Begründen Sie Ihre Antwort mit diesen relevanten Teilen der Videobeschreibung:
					{{information}}
								
				Geben Sie keine zusätzlichen Informationen an.
				Geben Sie keine Antworten zu anderen Themen, sondern schreiben Sie "Sorry, diese Frage kann ich nicht beantworten".
				Erzeugen Sie keinen weiteren Text, sondern schreiben Sie "Sorry, das ist eine Frage, die ich nicht beantworten kann".
				Wenn die Antwort nicht in den Videobeschreibungen gefunden werden kann, schreiben Sie "Sorry, ich konnte keine Antwort auf Ihre Frage finden.
				""");

		String information = relevantEmbeddings
				.stream()
				.map(match -> match.embedded().text() + ". " + URL + ": " + match.embedded().metadata(URL))
				.collect(Collectors.joining("\n\n"));

		Map<String, Object> variables = new HashMap<>();
		variables.put("question", question.getQuestion());
		variables.put("information", information);

		Prompt prompt = promptTemplate.apply(variables);

		if (this.chatModel != null) {
			this.chatModel.generate(prompt.toUserMessage().toString(), new CustomStreamingResponseHandler(question));
		} else {
			question.appendMessage("\n\nThe chat model is not ready yet... Please try again later.");
			question.completed();
		}
	}
}

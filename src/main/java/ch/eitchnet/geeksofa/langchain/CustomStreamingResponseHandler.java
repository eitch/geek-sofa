package ch.eitchnet.geeksofa.langchain;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomStreamingResponseHandler implements StreamingResponseHandler<AiMessage> {
	private static final Logger logger = LoggerFactory.getLogger(CustomStreamingResponseHandler.class);

	private final LangChainQuestion question;

	public CustomStreamingResponseHandler(LangChainQuestion question) {
		this.question = question;
	}

	@Override
	public void onNext(String token) {
		this.question.appendMessage(token);
	}

	@Override
	public void onComplete(Response<AiMessage> response) {
		this.question.appendMessage(
				"\n\nAnswer is complete for '" + question.getQuestion() + "', size: " + question.getAnswer().length());
		this.question.completed();
	}

	@Override
	public void onError(Throwable error) {
		logger.error("Error while receiving answer: {}", error.getMessage());
		question.appendMessage("\n\nSomething went wrong: " + error.getMessage());
		this.question.completed();
	}
}

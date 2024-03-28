package ch.eitchnet.geeksofa.chatbot;

import java.util.UUID;

public class ChatBotQuestion implements ChatBotListener {

	private final String id;

	private final ChatBotListener listener;
	private final String question;
	private volatile String answer;
	private volatile boolean completed;

	public ChatBotQuestion(ChatBotListener listener, String question) {
		this.id = UUID.randomUUID().toString();
		this.listener = listener;
		this.question = question;
		this.answer = "";
	}

	public String getId() {
		return this.id;
	}

	public String getQuestion() {
		return this.question;
	}

	public String getAnswer() {
		return this.answer;
	}

	@Override
	public void appendMessage(String message) {
		this.listener.appendMessage(message);
		this.answer += message;
	}

	public void addRelatedLink(String link) {
		this.listener.appendMessage("LINK: " + link);
	}

	public void completed() {
		this.listener.appendMessage("COMPLETED!");
		this.completed = true;
	}

	public boolean isOpen() {
		return this.completed;
	}

	public boolean isCompleted() {
		return this.completed;
	}
}

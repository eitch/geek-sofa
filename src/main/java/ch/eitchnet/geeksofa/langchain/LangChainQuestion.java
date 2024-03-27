package ch.eitchnet.geeksofa.langchain;

public class LangChainQuestion implements LangChainListener {

	private final LangChainListener listener;
	private final String question;
	private String answer;
	private boolean completed;

	public LangChainQuestion(LangChainListener listener, String question) {
		this.listener = listener;
		this.question = question;
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

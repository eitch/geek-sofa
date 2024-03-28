package ch.eitchnet.geeksofa.test;

import ch.eitchnet.geeksofa.chatbot.ChatBotHandler;
import ch.eitchnet.geeksofa.chatbot.ChatBotQuestion;
import li.strolch.testbase.runtime.RuntimeMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ChatBotHandlerTest {

	private static final Logger logger = LoggerFactory.getLogger(ChatBotHandlerTest.class);

	private static RuntimeMock mock;
	//private static Certificate cert;

	@BeforeClass
	public static void beforeClass() {
		mock = new RuntimeMock();
		mock.mockRuntime(new File("target/" + ChatBotHandlerTest.class.getSimpleName()),
				new File("src/test/resources/runtime"));
		mock.startContainer();
		//cert = mock.loginTest();
	}

	@AfterClass
	public static void afterClass() {
		if (mock != null)
			mock.destroyRuntime();
	}

	@Test
	public void shouldMarshallWithValidation() throws Exception {
		logger.info("Asking question...");
		ChatBotHandler chatBot = mock.getComponent(ChatBotHandler.class);
		ChatBotQuestion question = chatBot.ask("Was kannst du über Guido sagen?");

		long start = System.currentTimeMillis();
		while (!question.isCompleted()) {
			if (System.currentTimeMillis() - start > 30000) {
				break;
			}
		}

		if (!question.isCompleted())
			fail("Question didn't complete!");

		assertNotNull(question.getAnswer());
	}
}

package ch.eitchnet.geeksofa.test;

import ch.eitchnet.geeksofa.langchain.LangChainHandler;
import ch.eitchnet.geeksofa.langchain.LangChainQuestion;
import li.strolch.testbase.runtime.RuntimeMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class LangChainHandlerTest {

	private static final Logger logger = LoggerFactory.getLogger(LangChainHandlerTest.class);

	private static RuntimeMock mock;
	//private static Certificate cert;

	@BeforeClass
	public static void beforeClass() {
		mock = new RuntimeMock();
		mock.mockRuntime(new File("target/" + LangChainHandlerTest.class.getSimpleName()),
				new File("src/test/resources/runtime"));
		mock.startContainer();
		//cert = mock.loginTest();
	}

	@AfterClass
	public static void afterClass() {
		if (mock != null) {
			mock.destroyRuntime();
		}
	}

	@Test
	public void shouldMarshallWithValidation() throws Exception {
		logger.info("Asking question...");
		LangChainHandler langHandler = mock.getComponent(LangChainHandler.class);
		LangChainQuestion question = langHandler.ask("Was kannst du über Guido sagen?");

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

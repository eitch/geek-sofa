package ch.eitchnet.geeksofa.rest;

import ch.eitchnet.geeksofa.chatbot.ChatBotHandler;
import ch.eitchnet.geeksofa.chatbot.ChatBotQuestion;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import li.strolch.privilege.base.PrivilegeException;
import li.strolch.privilege.model.Certificate;
import li.strolch.privilege.model.PrivilegeContext;
import li.strolch.rest.RestfulStrolchComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static li.strolch.model.Tags.Json.ID;
import static li.strolch.model.Tags.Json.MSG;
import static li.strolch.rest.StrolchRestfulConstants.DATA;
import static li.strolch.rest.StrolchRestfulConstants.STROLCH_CERTIFICATE;
import static li.strolch.rest.helper.ResponseUtil.toResponse;
import static li.strolch.utils.helper.ExceptionHelper.getRootCauseMessage;

@Path("chatbot")
public class ChatBotResource {

	private static final Logger logger = LoggerFactory.getLogger(ChatBotResource.class);

	public static Certificate getCertificate(HttpServletRequest request) {
		Certificate cert = (Certificate) request.getAttribute(STROLCH_CERTIFICATE);
		if (cert == null)
			throw new IllegalStateException("Certificate missing on request as attribute " + STROLCH_CERTIFICATE);
		return cert;
	}

	@Path("ask")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response askQuestion(@Context HttpServletRequest request, String data) {
		RestfulStrolchComponent instance = RestfulStrolchComponent.getInstance();
		JsonObject resultJ = new JsonObject();
		try {
			Certificate cert = getCertificate(request);
			PrivilegeContext ctx = instance.getPrivilegeHandler().validate(cert);
			ctx.validateAction(PRIVILEGE_CHAT_BOT, PRIVILEGE_VALUE_ASK);

			JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
			String questionString = jsonObject.get("question").getAsString();

			ChatBotHandler chatBot = instance.getComponent(ChatBotHandler.class);

			if (chatBot.isNotReady()) {
				resultJ.addProperty(PARAM_COMPLETED, true);
				resultJ.addProperty(MSG, "Chatbot is not nicht bereit");
			} else {
				ChatBotQuestion question = chatBot.ask(questionString);
				resultJ.addProperty(ID, question.getId());
				resultJ.addProperty(PARAM_COMPLETED, question.isCompleted());
				resultJ.addProperty(MSG, question.getAnswer());
			}
		} catch (PrivilegeException e) {
			logger.error("Privilege Exception when accessing chat bot!", e);
			resultJ.addProperty(MSG, "Access Denied!");
		} catch (Exception e) {
			resultJ.addProperty(MSG, getRootCauseMessage(e));
		}

		return toResponse(DATA, resultJ);
	}
}

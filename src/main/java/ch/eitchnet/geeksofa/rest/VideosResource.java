package ch.eitchnet.geeksofa.rest;

import ch.eitchnet.geeksofa.search.VideoSearch;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.helper.ResponseUtil;
import li.strolch.utils.collections.Paging;

import static ch.eitchnet.geeksofa.model.Constants.PARAM_START_TIME;
import static ch.eitchnet.geeksofa.model.JsonVisitors.videoToJson;

@Path("videos")
public class VideosResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@Context HttpServletRequest request, @QueryParam("query") String query,
			@DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("3") @QueryParam("limit") int limit)
			throws Exception {

		RestfulStrolchComponent instance = RestfulStrolchComponent.getInstance();
		Paging<Resource> paging = instance.getAgent().runAsAgentWithResult(ctx -> {
			try (StrolchTransaction tx = instance.openTx(ctx.getCertificate(), getClass())) {
				return new VideoSearch()
						.stringQuery(query)
						.search(tx)
						.orderByParam(PARAM_START_TIME)
						.toPaging(offset, limit);
			}
		});

		return ResponseUtil.toResponse(paging, e -> e.accept(videoToJson()));
	}
}

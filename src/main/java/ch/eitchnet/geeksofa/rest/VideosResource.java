package ch.eitchnet.geeksofa.rest;

import ch.eitchnet.geeksofa.search.VideoSearch;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.privilege.model.Certificate;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.helper.ResponseUtil;
import li.strolch.utils.collections.Paging;

import static ch.eitchnet.geeksofa.model.Constants.TYPE_VIDEO;
import static ch.eitchnet.geeksofa.model.JsonVisitors.videoToJson;
import static li.strolch.rest.StrolchRestfulConstants.DATA;
import static li.strolch.rest.StrolchRestfulConstants.STROLCH_CERTIFICATE;

@Path("videos")
public class VideosResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@Context HttpServletRequest request, @QueryParam("query") String query,
			@DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("20") @QueryParam("limit") int limit)
			throws Exception {

		RestfulStrolchComponent instance = RestfulStrolchComponent.getInstance();
		Paging<Resource> paging = instance.getAgent().runAsAgentWithResult(ctx -> {
			try (StrolchTransaction tx = instance.openTx(ctx.getCertificate(), getClass())) {
				// perform a book search
				return new VideoSearch() //
						.stringQuery(query) //
						.search(tx) //
						.orderByName(false) //
						.toPaging(offset, limit);
			}
		});

		return ResponseUtil.toResponse(paging, e -> e.accept(videoToJson()));
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request, @PathParam("id") String id) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(STROLCH_CERTIFICATE);

		// open the TX with the certificate, using this class as context
		try (StrolchTransaction tx = RestfulStrolchComponent.getInstance().openTx(cert, getClass())) {

			// get the book
			Resource book = tx.getResourceBy(TYPE_VIDEO, id, true);

			// transform to JSON
			JsonObject bookJ = book.accept(videoToJson());

			// return
			return ResponseUtil.toResponse(DATA, bookJ);
		}
	}
}

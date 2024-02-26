package ch.eitchnet.geeksofa.rest;

import ch.eitchnet.geeksofa.search.VideoSearch;
import com.google.gson.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.helper.ResponseUtil;
import li.strolch.utils.collections.Paging;
import li.strolch.utils.iso8601.ISO8601;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static ch.eitchnet.geeksofa.model.JsonVisitors.videoToJson;
import static ch.eitchnet.geeksofa.model.VideosHelper.daysBetweenIgnoreYear;
import static ch.eitchnet.geeksofa.model.VideosHelper.findClosestVideo;
import static li.strolch.rest.StrolchRestfulConstants.DATA;
import static li.strolch.rest.StrolchRestfulConstants.SIZE;

@Path("videos")
public class VideosResource {

	@Path("meta")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMetaInformation() throws Exception {
		RestfulStrolchComponent instance = RestfulStrolchComponent.getInstance();
		return instance.getAgent().runAsAgentWithResult(ctx -> {
			try (StrolchTransaction tx = instance.openTx(ctx.getCertificate(), getClass())) {
				ZonedDateTime[] earliestStartTime = new ZonedDateTime[1];
				ZonedDateTime[] latestStartTime = new ZonedDateTime[1];
				tx.streamResources(TYPE_VIDEO).forEach(video -> {
					ZonedDateTime startTime = video.getDate(PARAM_START_TIME);
					if (earliestStartTime[0] == null || earliestStartTime[0].isAfter(startTime))
						earliestStartTime[0] = startTime;
					if (latestStartTime[0] == null || latestStartTime[0].isBefore(startTime))
						latestStartTime[0] = startTime;
				});

				JsonObject resultJ = new JsonObject();
				resultJ.addProperty(SIZE, tx.getResourceCount(TYPE_VIDEO));
				resultJ.addProperty(PARAM_EARLIEST_START_TIME, ISO8601.toString(earliestStartTime[0]));
				resultJ.addProperty(PARAM_LATEST_START_TIME, ISO8601.toString(latestStartTime[0]));
				return ResponseUtil.toResponse(DATA, resultJ);
			}
		});
	}

	@Path("spotlight")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSpotlight() throws Exception {
		RestfulStrolchComponent instance = RestfulStrolchComponent.getInstance();
		return instance.getAgent().runAsAgentWithResult(ctx -> {
			try (StrolchTransaction tx = instance.openTx(ctx.getCertificate(), getClass())) {
				LocalDate today = LocalDate.now();
				List<Resource> videos = findClosestVideo(tx.streamResources(TYPE_VIDEO), today);
				if (videos.isEmpty())
					return ResponseUtil.toResponse();

				Resource video = videos.get(new SecureRandom().nextInt(videos.size()));

				ZonedDateTime startTime = video.getDate(PARAM_START_TIME);
				int yearsAgo = ZonedDateTime.now().getYear() - startTime.getYear();
				int daysBetween = daysBetweenIgnoreYear(today, startTime.toLocalDate(), true);

				JsonObject videoJ = video.accept(videoToJson());
				videoJ.addProperty(PARAM_DAYS_AGO, Math.abs(daysBetween));
				videoJ.addProperty(PARAM_DAYS_NEGATIVE, daysBetween < 0);
				videoJ.addProperty(PARAM_YEARS_AGO, yearsAgo);
				return ResponseUtil.toResponse(DATA, videoJ);
			}
		});
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@QueryParam("query") String query, @QueryParam("fromDate") String fromDate,
			@QueryParam("toDate") String toDate, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("3") @QueryParam("limit") int limit) throws Exception {

		RestfulStrolchComponent instance = RestfulStrolchComponent.getInstance();
		Paging<Resource> paging = instance.getAgent().runAsAgentWithResult(ctx -> {
			try (StrolchTransaction tx = instance.openTx(ctx.getCertificate(), getClass())) {
				return new VideoSearch()
						.stringQuery(query, fromDate, toDate)
						.search(tx)
						.orderByParam(PARAM_START_TIME)
						.toPaging(offset, limit, tx.getResourceCount(TYPE_VIDEO));
			}
		});

		return ResponseUtil.toResponse(paging, e -> e.accept(videoToJson()));
	}
}

package ch.eitchnet.geeksofa.search;

import li.strolch.search.ResourceSearch;
import li.strolch.utils.iso8601.ISO8601;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static li.strolch.utils.helper.StringHelper.isEmpty;
import static li.strolch.utils.helper.StringHelper.isNotEmpty;
import static li.strolch.utils.iso8601.ISO8601.*;

public class VideoSearch extends ResourceSearch {

	public VideoSearch() {
		types(TYPE_VIDEO);
	}

	public VideoSearch stringQuery(String value, String fromDate, String toDate) {
		if (isNotEmpty(value)) {

			// split by spaces
			value = value.trim();
			String[] values = value.split(" ");

			where(param(PARAM_TITLE)
					.containsIgnoreCase(values)
					.or(param(PARAM_DESCRIPTION).containsIgnoreCase(values)));
		}

		if (isNotEmpty(fromDate))
			where(param(PARAM_START_TIME).isAfter(parseToZdt(fromDate), true));
		if (isNotEmpty(toDate))
			where(param(PARAM_START_TIME).isBefore(parseToZdt(toDate), true));

		return this;
	}
}

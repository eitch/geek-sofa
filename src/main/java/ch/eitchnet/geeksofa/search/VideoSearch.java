package ch.eitchnet.geeksofa.search;

import static ch.eitchnet.geeksofa.model.Constants.*;
import static li.strolch.utils.helper.StringHelper.isEmpty;

import li.strolch.search.ResourceSearch;

public class VideoSearch extends ResourceSearch {

	public VideoSearch() {
		types(TYPE_VIDEO);
	}

	public VideoSearch stringQuery(String value) {
		if (isEmpty(value))
			return this;

		// split by spaces
		value = value.trim();
		String[] values = value.split(" ");

		where(param(PARAM_TITLE).containsIgnoreCase(values).or(param(PARAM_DESCRIPTION).containsIgnoreCase(values)));

		return this;
	}
}

package ch.eitchnet.geeksofa.web;

import li.strolch.rest.filters.AuthenticationRequestFilter;

import java.util.Set;

public class CustomAuthenticationRequestFilter extends AuthenticationRequestFilter {

	@Override
	protected Set<String> getUnsecuredPaths() {
		Set<String> paths = super.getUnsecuredPaths();
		paths.add("videos/meta");
		paths.add("videos/spotlight");
		paths.add("videos/search");
		return paths;
	}
}

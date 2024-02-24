package ch.eitchnet.geeksofa.model;

import li.strolch.model.json.StrolchRootElementToJsonVisitor;

public class JsonVisitors {

	public static StrolchRootElementToJsonVisitor toJson() {
		return new StrolchRootElementToJsonVisitor().withoutPolicies();
	}

	public static StrolchRootElementToJsonVisitor flatToJson() {
		return toJson().withoutVersion().flat();
	}

	public static StrolchRootElementToJsonVisitor videoToJson() {
		return flatToJson();
	}
}

package de.ellpeck.rockbottom.mod.util.version;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class SemanticVersionPredicateParser {
	private static final Map<String, Function<SemanticVersionImpl, Predicate<SemanticVersionImpl>>> PREFIXES;

	public static Predicate<SemanticVersionImpl> create(String text) throws VersionParsingException {
		List<Predicate<SemanticVersionImpl>> predicateList = new ArrayList<>();
		List<SemanticVersionImpl> prereleaseVersions = new ArrayList<>();

		for (String s : text.split(" ")) {
			s = s.trim();
			if (s.isEmpty() || s.equals("*")) {
				continue;
			}

			Function<SemanticVersionImpl, Predicate<SemanticVersionImpl>> factory = null;
			for (String prefix : PREFIXES.keySet()) {
				if (s.startsWith(prefix)) {
					factory = PREFIXES.get(prefix);
					s = s.substring(prefix.length());
					break;
				}
			}

			SemanticVersionImpl version = new SemanticVersionImpl(s, true);
			if (version.isPrerelease()) {
				prereleaseVersions.add(version);
			}

			if (factory == null) {
				factory = PREFIXES.get("=");
			} else if (version.hasWildcard()) {
				throw new VersionParsingException("Prefixed ranges are not allowed to use X-ranges!");
			}

			predicateList.add(factory.apply(version));
		}

		if (predicateList.isEmpty()) {
			return (s) -> true;
		}

		return (s) -> {
			for (Predicate<SemanticVersionImpl> p : predicateList) {
				if (!p.test(s)) {
					return false;
				}
			}

			return true;
		};
	}

	static {
		// Make sure to keep this sorted in order of length!
		PREFIXES = new LinkedHashMap<>();
		PREFIXES.put(">=", (target) -> (source) -> source.compareTo(target) >= 0);
		PREFIXES.put("<=", (target) -> (source) -> source.compareTo(target) <= 0);
		PREFIXES.put(">", (target) -> (source) -> source.compareTo(target) > 0);
		PREFIXES.put("<", (target) -> (source) -> source.compareTo(target) < 0);
		PREFIXES.put("=", (target) -> (source) -> source.compareTo(target) == 0);
		PREFIXES.put("~", (target) -> (source) -> source.compareTo(target) >= 0
				&& source.getVersionComponent(0) == target.getVersionComponent(0)
				&& source.getVersionComponent(1) == target.getVersionComponent(1));
		PREFIXES.put("^", (target) -> (source) -> source.compareTo(target) >= 0
				&& source.getVersionComponent(0) == target.getVersionComponent(0));
	}
}
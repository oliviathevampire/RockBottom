package de.ellpeck.rockbottom.mod.util.version;

import java.util.function.Predicate;

public final class StringVersionPredicateParser {
	public static Predicate<StringVersion> create(String text) throws VersionParsingException {
		final String compared = text.trim();

		if (compared.equals("*")) {
			return (t) -> true;
		} else {
			return (t) -> compared.equals(t.getFriendlyString());
		}
	}
}
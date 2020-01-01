package de.ellpeck.rockbottom.mod.util.version;

import de.ellpeck.rockbottom.mod.loader.Version;

import java.util.function.Predicate;

@FunctionalInterface
public interface VersionPredicateParser<E extends Version> {
	/**
	 * Parse and create a predicate comparing given Version objects.
	 *
	 * @param s The predicate string. Guaranteed to be non-null and non-empty.
	 * @return The resulting predicate.
	 */
	Predicate<E> create(String s);

	static boolean matches(Version version, String s) throws VersionParsingException {
		if (version instanceof SemanticVersionImpl) {
			return SemanticVersionPredicateParser.create(s).test((SemanticVersionImpl) version);
		} else if (version instanceof StringVersion) {
			return StringVersionPredicateParser.create(s).test((StringVersion) version);
		} else {
			throw new VersionParsingException("Unknown version type!");
		}
	}
}
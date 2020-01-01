package de.ellpeck.rockbottom.mod.util.version;

import de.ellpeck.rockbottom.mod.loader.SemanticVersion;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class SemanticVersionImpl implements SemanticVersion {
	private static final Pattern DOT_SEPARATED_ID = Pattern.compile("|[-0-9A-Za-z]+(\\.[-0-9A-Za-z]+)*");
	private final int[] components;
	private final String prerelease;
	private final String build;
	private String friendlyName;

	public SemanticVersionImpl(String version, boolean storeX) throws VersionParsingException {
		int buildDelimPos = version.indexOf('+');
		if (buildDelimPos >= 0) {
			build = version.substring(buildDelimPos + 1);
			version = version.substring(0, buildDelimPos);
		} else {
			build = null;
		}

		int dashDelimPos = version.indexOf('-');
		if (dashDelimPos >= 0) {
			prerelease = version.substring(dashDelimPos + 1);
			version = version.substring(0, dashDelimPos);
		} else {
			prerelease = null;
		}

		if (prerelease != null && !DOT_SEPARATED_ID.matcher(prerelease).matches()) {
			throw new VersionParsingException("Invalid prerelease string '" + prerelease + "'!");
		}

		if (build != null && !DOT_SEPARATED_ID.matcher(build).matches()) {
			throw new VersionParsingException("Invalid build string '" + build + "'!");
		}

		if (version.endsWith(".")) {
			throw new VersionParsingException("Negative version number component found!");
		} else if (version.startsWith(".")) {
			throw new VersionParsingException("Missing version component!");
		}

		String[] componentStrings = version.split("\\.");
		if (componentStrings.length < 1) {
			throw new VersionParsingException("Did not provide version numbers!");
		}

		components = new int[componentStrings.length];

		for (int i = 0; i < componentStrings.length; i++) {
			String compStr = componentStrings[i];

			if (storeX) {
				if (compStr.equals("x") || compStr.equals("X") || compStr.equals("*")) {
					if (prerelease != null) {
						throw new VersionParsingException("Pre-release versions are not allowed to use X-ranges!");
					}

					components[i] = Integer.MIN_VALUE;
					continue;
				} else if (i > 0 && components[i - 1] == Integer.MIN_VALUE) {
					throw new VersionParsingException("Interjacent wildcard (1.x.2) are disallowed!");
				}
			}

			if (compStr.trim().isEmpty()) {
				throw new VersionParsingException("Missing version number component!");
			}

			try {
				components[i] = Integer.parseInt(compStr);
				if (components[i] < 0) {
					throw new VersionParsingException("Negative version number component '" + compStr + "'!");
				}
			} catch (NumberFormatException e) {
				throw new VersionParsingException("Could not parse version number component '" + compStr + "'!", e);
			}
		}

		if (storeX && components.length == 1 && components[0] == Integer.MIN_VALUE) {
			throw new VersionParsingException("Versions of form 'x' or 'X' not allowed!");
		}

		buildFriendlyName();
	}

	private void buildFriendlyName() {
		StringBuilder fnBuilder = new StringBuilder();
		boolean first = true;

		for (int i : components) {
			if (first) {
				first = false;
			} else {
				fnBuilder.append('.');
			}

			if (i == Integer.MIN_VALUE) {
				fnBuilder.append('x');
			} else {
				fnBuilder.append(i);
			}
		}

		if (prerelease != null) {
			fnBuilder.append('-').append(prerelease);
		}

		if (build != null) {
			fnBuilder.append('+').append(build);
		}

		friendlyName = fnBuilder.toString();
	}

	@Override
	public int getVersionComponentCount() {
		return components.length;
	}

	@Override
	public int getVersionComponent(int pos) {
		if (pos < 0) {
			throw new RuntimeException("Tried to access negative version number component!");
		} else if (pos >= components.length) {
			// Repeat "x" if x-range, otherwise repeat "0".
			return components[components.length - 1] == Integer.MIN_VALUE ? Integer.MIN_VALUE : 0;
		} else {
			return components[pos];
		}
	}

	@Override
	public Optional<String> getPrereleaseKey() {
		return Optional.ofNullable(prerelease);
	}

	@Override
	public Optional<String> getBuildKey() {
		return Optional.ofNullable(build);
	}

	@Override
	public String getFriendlyString() {
		return friendlyName;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SemanticVersionImpl)) {
			return false;
		} else {
			SemanticVersionImpl other = (SemanticVersionImpl) o;
			if (!equalsComponentsExactly(other)) {
				return false;
			}

			return Objects.equals(prerelease, other.prerelease) && Objects.equals(build, other.build);
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(components) * 73 + (prerelease != null ? prerelease.hashCode() * 11 : 0) + (build != null ? build.hashCode() : 0);
	}

	@Override
	public String toString() {
		return getFriendlyString();
	}

	@Override
	public boolean hasWildcard() {
		for (int i : components) {
			if (i < 0) {
				return true;
			}
		}

		return false;
	}

	public boolean equalsComponentsExactly(SemanticVersionImpl other) {
		for (int i = 0; i < Math.max(getVersionComponentCount(), other.getVersionComponentCount()); i++) {
			if (getVersionComponent(i) != other.getVersionComponent(i)) {
				return false;
			}
		}

		return true;
	}

	boolean isPrerelease() {
		return prerelease != null;
	}
}
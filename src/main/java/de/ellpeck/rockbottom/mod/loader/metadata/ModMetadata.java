package de.ellpeck.rockbottom.mod.loader.metadata;

import com.google.gson.JsonElement;
import de.ellpeck.rockbottom.mod.loader.Version;

import java.util.Collection;
import java.util.Optional;

public interface ModMetadata {
	String getType();

	// When adding getters, follow the order as presented on the wiki.
	// No defaults.

	String getId();
	Version getVersion();

	Collection<ModDependency> getDepends();
	Collection<ModDependency> getRecommends();
	Collection<ModDependency> getSuggests();
	Collection<ModDependency> getConflicts();
	Collection<ModDependency> getBreaks();

	String getName();
	String getDescription();
	Collection<Person> getAuthors();
	Collection<Person> getContributors();
	ContactInformation getContact();
	Collection<String> getLicense();

	/**
	 * Get the path to an icon.
	 *
	 * The standard defines icons as square .PNG files, however their
	 * dimensions are not defined - in particular, they are not
	 * guaranteed to be a power of two.
	 *
	 * The preferred size is used in the following manner:
	 * - the smallest image larger than or equal to the size
	 *   is returned, if one is present;
	 * - failing that, the largest image is returned.
	 *
	 * @param size The preferred size.
	 * @return The icon path, if any.
	 */
	Optional<String> getIconPath(int size);

	boolean containsCustomValue(String key);
	CustomValue getCustomValue(String key);

	/**
	 * @deprecated Use {@link #containsCustomValue} instead, this will be removed (can't expose GSON types)!
	 */
	@Deprecated
	boolean containsCustomElement(String key);

	/**
	 * @deprecated Use {@link #getCustomValue} instead, this will be removed (can't expose GSON types)!
	 */
	@Deprecated
	JsonElement getCustomElement(String key);
}
package de.ellpeck.rockbottom.mod.metadata;

import de.ellpeck.rockbottom.mod.loader.Version;
import de.ellpeck.rockbottom.mod.loader.metadata.*;
import de.ellpeck.rockbottom.mod.util.version.VersionDeserializer;
import de.ellpeck.rockbottom.mod.util.version.VersionParsingException;

import java.util.*;
import java.util.Map.Entry;

public final class BuiltinModMetadata extends AbstractModMetadata {
	private final String id;
	private final Version version;
	private final String name;
	private final String description;
	private final Collection<Person> authors;
	private final Collection<Person> contributors;
	private final ContactInformation contact;
	private final Collection<String> license;
	private final NavigableMap<Integer, String> icons;

	private BuiltinModMetadata(String id, Version version,
			String name, String description,
			Collection<Person> authors, Collection<Person> contributors,
			ContactInformation contact,
			Collection<String> license,
			NavigableMap<Integer, String> icons) {
		this.id = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.authors = authors;
		this.contributors = contributors;
		this.contact = contact;
		this.license = license;
		this.icons = icons;
	}

	@Override
	public String getType() {
		return "builtin";
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Collection<Person> getAuthors() {
		return authors;
	}

	@Override
	public Collection<Person> getContributors() {
		return contributors;
	}

	@Override
	public ContactInformation getContact() {
		return contact;
	}

	@Override
	public Collection<String> getLicense() {
		return license;
	}

	@Override
	public Optional<String> getIconPath(int size) {
		if (icons.isEmpty()) return Optional.empty();

		Integer key = size;
		Entry<Integer, String> ret = icons.ceilingEntry(key);
		if (ret == null) ret = icons.lastEntry();

		return Optional.of(ret.getValue());
	}

	@Override
	public Collection<ModDependency> getDepends() { return Collections.emptyList(); }
	@Override
	public Collection<ModDependency> getRecommends() { return Collections.emptyList(); }
	@Override
	public Collection<ModDependency> getSuggests() { return Collections.emptyList(); }
	@Override
	public Collection<ModDependency> getConflicts() { return Collections.emptyList(); }
	@Override
	public Collection<ModDependency> getBreaks() { return Collections.emptyList(); }
	@Override
	public boolean containsCustomValue(String key) { return false; }
	@Override
	public CustomValue getCustomValue(String key) { return null; }

	public static class Builder {
		private final String id;
		private final Version version;
		private String name;
		private String description = "";
		private final Collection<Person> authors = new ArrayList<>();
		private final Collection<Person> contributors = new ArrayList<>();
		private ContactInformation contact = ContactInformation.EMPTY;
		private final Collection<String> license = new ArrayList<>();
		private final NavigableMap<Integer, String> icons = new TreeMap<>();

		public Builder(String id, String version) {
			this.name = this.id = id;

			try {
				this.version = VersionDeserializer.deserializeSemantic(version);
			} catch (VersionParsingException e) {
				throw new RuntimeException(e);
			}
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder addAuthor(String name, Map<String, String> contactMap) {
			this.authors.add(createPerson(name, contactMap));
			return this;
		}

		public Builder addContributor(String name, Map<String, String> contactMap) {
			this.contributors.add(createPerson(name, contactMap));
			return this;
		}

		public Builder setContact(ContactInformation contact) {
			this.contact = contact;
			return this;
		}

		public Builder addLicense(String license) {
			this.license.add(license);
			return this;
		}

		public Builder addIcon(int size, String path) {
			this.icons.put(size, path);
			return this;
		}

		public ModMetadata build() {
			return new BuiltinModMetadata(id, version, name, description, authors, contributors, contact, license, icons);
		}

		private static Person createPerson(String name, Map<String, String> contactMap) {
			return new Person() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public ContactInformation getContact() {
					return contact;
				}

				private final ContactInformation contact = contactMap.isEmpty() ? ContactInformation.EMPTY : new MapBackedContactInformation(contactMap);
			};
		}
	}
}
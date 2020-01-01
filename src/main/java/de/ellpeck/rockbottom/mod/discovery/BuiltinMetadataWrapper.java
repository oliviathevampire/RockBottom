package de.ellpeck.rockbottom.mod.discovery;

import de.ellpeck.rockbottom.mod.loader.Version;
import de.ellpeck.rockbottom.mod.loader.api.EnvType;
import de.ellpeck.rockbottom.mod.loader.metadata.*;
import de.ellpeck.rockbottom.mod.metadata.AbstractModMetadata;
import de.ellpeck.rockbottom.mod.metadata.EntrypointMetadata;
import de.ellpeck.rockbottom.mod.metadata.LoaderModMetadata;
import de.ellpeck.rockbottom.mod.metadata.NestedJarEntry;
import org.apache.logging.log4j.Logger;

import java.util.*;

class BuiltinMetadataWrapper extends AbstractModMetadata implements LoaderModMetadata {
	private final ModMetadata parent;

	public BuiltinMetadataWrapper(ModMetadata parent) {
		this.parent = parent;
	}

	@Override
	public String getType() { return parent.getType(); }
	@Override
	public String getId() { return parent.getId(); }
	@Override
	public Version getVersion() { return parent.getVersion(); }
	@Override
	public Collection<ModDependency> getDepends() { return parent.getDepends(); }
	@Override
	public Collection<ModDependency> getRecommends() { return parent.getRecommends(); }
	@Override
	public Collection<ModDependency> getSuggests() { return parent.getSuggests(); }
	@Override
	public Collection<ModDependency> getConflicts() { return parent.getConflicts(); }
	@Override
	public Collection<ModDependency> getBreaks() { return parent.getBreaks(); }
	@Override
	public String getName() { return parent.getName(); }
	@Override
	public String getDescription() { return parent.getDescription(); }
	@Override
	public Collection<Person> getAuthors() { return parent.getAuthors(); }
	@Override
	public Collection<Person> getContributors() { return parent.getContributors(); }
	@Override
	public ContactInformation getContact() { return parent.getContact(); }
	@Override
	public Collection<String> getLicense() { return parent.getLicense(); }
	@Override
	public Optional<String> getIconPath(int size) { return parent.getIconPath(size); }
	@Override
	public boolean containsCustomValue(String key) { return parent.containsCustomValue(key); }
	@Override
	public CustomValue getCustomValue(String key) { return parent.getCustomValue(key); }
	@Override
	public int getSchemaVersion() { return Integer.MAX_VALUE; }
	@Override
	public Map<String, String> getLanguageAdapterDefinitions() { return Collections.emptyMap(); }
	@Override
	public Collection<NestedJarEntry> getJars() { return Collections.emptyList(); }
	@Override
	public Collection<String> getMixinConfigs(EnvType type) { return Collections.emptyList(); }
	@Override
	public boolean loadsInEnvironment(EnvType type) { return true; }
	@Override
	public Collection<String> getOldInitializers() { return Collections.emptyList(); }
	@Override
	public List<EntrypointMetadata> getEntrypoints(String type) { return Collections.emptyList(); }
	@Override
	public Collection<String> getEntrypointKeys() { return Collections.emptyList(); }
	@Override
	public void emitFormatWarnings(Logger logger) { }
}
package de.ellpeck.rockbottom.mod.loader;

public interface LanguageAdapter {
	<T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException;
}
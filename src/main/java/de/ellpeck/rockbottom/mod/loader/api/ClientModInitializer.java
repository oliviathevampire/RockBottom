package de.ellpeck.rockbottom.mod.loader.api;

@FunctionalInterface
public interface ClientModInitializer {
	void onInitializeClient();
}
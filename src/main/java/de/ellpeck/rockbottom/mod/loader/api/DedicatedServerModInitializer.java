package de.ellpeck.rockbottom.mod.loader.api;

@FunctionalInterface
public interface DedicatedServerModInitializer {
	void onInitializeServer();
}
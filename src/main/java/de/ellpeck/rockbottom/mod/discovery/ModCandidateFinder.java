package de.ellpeck.rockbottom.mod.discovery;

import java.net.URL;
import java.util.function.Consumer;

@FunctionalInterface
public interface ModCandidateFinder {
	void findCandidates(Consumer<URL> urlProposer);
}
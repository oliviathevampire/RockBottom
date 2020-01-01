package de.ellpeck.rockbottom.mod.discovery;

public class ModResolutionException extends Exception {
	public ModResolutionException(String s) {
		super(s);
	}

	public ModResolutionException(Throwable t) {
		super(t);
	}

	public ModResolutionException(String s, Throwable t) {
		super(s, t);
	}
}
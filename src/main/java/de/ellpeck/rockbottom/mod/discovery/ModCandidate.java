package de.ellpeck.rockbottom.mod.discovery;

import de.ellpeck.rockbottom.mod.metadata.LoaderModMetadata;

import java.net.URL;

public class ModCandidate {
	private final LoaderModMetadata info;
	private final URL originUrl;
	private final int depth;

	public ModCandidate(LoaderModMetadata info, URL originUrl, int depth) {
		this.info = info;
		this.originUrl = originUrl;
		this.depth = depth;
	}

	public URL getOriginUrl() {
		return originUrl;
	}

	public LoaderModMetadata getInfo() {
		return info;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ModCandidate)) {
			return false;
		} else {
			ModCandidate other = (ModCandidate) obj;
			return other.info.getVersion().getFriendlyString().equals(info.getVersion().getFriendlyString()) && other.info.getId().equals(info.getId());
		}
	}

	@Override
	public int hashCode() {
		return info.getId().hashCode() * 17 + info.getVersion().hashCode();
	}

	@Override
	public String toString() {
		return "ModCandidate{" + info.getId() + "@" + info.getVersion().getFriendlyString() + "}";
	}
}
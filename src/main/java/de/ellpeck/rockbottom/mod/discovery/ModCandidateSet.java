package de.ellpeck.rockbottom.mod.discovery;

import com.google.common.base.Joiner;
import de.ellpeck.rockbottom.mod.loader.Version;

import java.util.*;
import java.util.stream.Collectors;

public class ModCandidateSet {
	private final String modId;
	private final Set<ModCandidate> depthZeroCandidates = new HashSet<>();
	private final Map<String, ModCandidate> candidates = new HashMap<>();

	private static int compare(ModCandidate a, ModCandidate b) {
		Version av = a.getInfo().getVersion();
		Version bv = b.getInfo().getVersion();

		if (av instanceof Comparable && bv instanceof Comparable) {
			//noinspection unchecked
			return ((Comparable) bv).compareTo(av);
		} else {
			return 0;
		}
	}

	public ModCandidateSet(String modId) {
		this.modId = modId;
	}

	public String getModId() {
		return modId;
	}

	public boolean add(ModCandidate candidate) {
		String version = candidate.getInfo().getVersion().getFriendlyString();
		ModCandidate oldCandidate = candidates.get(version);
		if (oldCandidate != null) {
			int oldDepth = oldCandidate.getDepth();
			int newDepth = candidate.getDepth();

			if (oldDepth <= newDepth) {
				return false;
			} else {
				candidates.remove(version);
				if (oldDepth > 0) {
					depthZeroCandidates.remove(oldCandidate);
				}
			}
		}

		candidates.put(version, candidate);
		if (candidate.getDepth() == 0) {
			depthZeroCandidates.add(candidate);
		}

		return true;
	}

	public boolean isUserProvided() {
		return !depthZeroCandidates.isEmpty();
	}

	public Collection<ModCandidate> toSortedSet() throws ModResolutionException {
		if (depthZeroCandidates.size() > 1) {
			Set<String> modVersionStrings = depthZeroCandidates.stream()
				.map((c) -> "[" + c.getInfo().getVersion() + " at " + c.getOriginUrl().getFile() + "]")
				.collect(Collectors.toSet());

			throw new ModResolutionException("Duplicate versions for mod ID '" + modId + "': " + Joiner.on(", ").join(modVersionStrings));
		} else if (depthZeroCandidates.size() == 1) {
			return depthZeroCandidates;
		} else if (candidates.size() > 1) {
			List<ModCandidate> out = new ArrayList<>(candidates.values());
			out.sort(ModCandidateSet::compare);
			return out;
		} else {
			return Collections.singleton(candidates.values().iterator().next());
		}
	}
}
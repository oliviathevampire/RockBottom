package de.ellpeck.rockbottom.mod.util;

import java.util.*;

public final class Arguments {
	private final Map<String, String> values;
	private final List<String> extraArgs;

	public Arguments() {
		values = new LinkedHashMap<>();
		extraArgs = new ArrayList<>();
	}

	public Collection<String> keys() {
		return values.keySet();
	}

	public List<String> getExtraArgs() {
		return Collections.unmodifiableList(extraArgs);
	}

	public boolean containsKey(String key) {
		return values.containsKey(key);
	}

	public String get(String key) {
		return values.get(key);
	}

	public String getOrDefault(String key, String value) {
		return values.getOrDefault(key, value);
	}

	public void put(String key, String value) {
		values.put(key, value);
	}

	public void addExtraArg(String value) {
		extraArgs.add(value);
	}

	public void parse(String[] args) {
		parse(Arrays.asList(args));
	}

	public void parse(List<String> args) {
		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i);
			if (arg.startsWith("--") && i < args.size() - 1) {
				values.put(arg.substring(2), args.get(++i));
			} else {
				extraArgs.add(arg);
			}
		}
	}

	public String[] toArray() {
		String[] newArgs = new String[values.size() * 2 + extraArgs.size()];
		int i = 0;
		for (String s : values.keySet()) {
			newArgs[i++] = "--" + s;
			newArgs[i++] = values.get(s);
		}
		for (String s : extraArgs) {
			newArgs[i++] = s;
		}
		return newArgs;
	}

	public String remove(String s) {
		return values.remove(s);
	}
}
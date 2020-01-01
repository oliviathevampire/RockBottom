package de.ellpeck.rockbottom.mod.metadata;

import com.google.gson.*;
import de.ellpeck.rockbottom.mod.loader.metadata.CustomValue;
import de.ellpeck.rockbottom.mod.loader.metadata.ModMetadata;

import java.util.Map;

public abstract class AbstractModMetadata implements ModMetadata {
	@Override
	public boolean containsCustomElement(String key) {
		return containsCustomValue(key);
	}

	@Override
	public JsonElement getCustomElement(String key) {
		CustomValue value = getCustomValue(key);

		return value != null ? convert(value) : null;
	}

	private static JsonElement convert(CustomValue value) {
		switch (value.getType()) {
		case ARRAY: {
			JsonArray ret = new JsonArray();

			for (CustomValue v : value.getAsArray()) {
				ret.add(convert(v));
			}

			return ret;
		}
		case BOOLEAN:
			return new JsonPrimitive(value.getAsBoolean());
		case NULL:
			return JsonNull.INSTANCE;
		case NUMBER:
			return new JsonPrimitive(value.getAsNumber());
		case OBJECT: {
			JsonObject ret = new JsonObject();

			for (Map.Entry<String, CustomValue> entry : value.getAsObject()) {
				ret.add(entry.getKey(), convert(entry.getValue()));
			}

			return ret;
		}
		case STRING:
			return new JsonPrimitive(value.getAsString());
		}

		throw new IllegalStateException();
	}
}
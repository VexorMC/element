package net.minecraft.resources;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileToIdConverter {
	private final String prefix;
	private final String extension;

	public FileToIdConverter(String string, String string2) {
		this.prefix = string;
		this.extension = string2;
	}

	public static FileToIdConverter json(String string) {
		return new FileToIdConverter(string, ".json");
	}

	public Identifier idToFile(Identifier resourceLocation) {
		return resourceLocation.withPath(this.prefix + "/" + resourceLocation.getPath() + this.extension);
	}

	public Identifier fileToId(Identifier resourceLocation) {
		String string = resourceLocation.getPath();
		System.out.printf("-- %s %s%n", string, this.extension);
		return resourceLocation.withPath(string.substring(this.prefix.length() + 1, string.length() - this.extension.length()));
	}

	public Map<Identifier, Resource> listMatchingResources(ResourceManager resourceManager) {
        return resourceManager.listResources(id -> id.getPath().startsWith(prefix) && id.getPath().endsWith(this.extension));
    }
}

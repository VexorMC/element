package net.minecraft.resources;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		return resourceLocation.withPath(string.substring(this.prefix.length() + 1, string.length() - this.extension.length()));
	}

	public Map<Identifier, List<Resource>> listMatchingResourceStacks(ResourceManager resourceManager) {
        try {
            return resourceManager.getAllResources(new Identifier(this.prefix)).stream().map(
				resource -> {
					Identifier id = this.fileToId(resource.getId());
					return Map.entry(id, resource);
				}
			).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

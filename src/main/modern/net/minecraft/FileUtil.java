package net.minecraft;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class FileUtil {
    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_FILE_NAME = 255;
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern STRICT_PATH_SEGMENT_CHECK = Pattern.compile("[-._a-z0-9]+");


    public static boolean isPathNormalized(Path path) {
        Path path2 = path.normalize();
        return path2.equals(path);
    }

    public static boolean isPathPortable(Path path) {
        for (Path path2 : path) {
            if (!isPathPartPortable(path2.toString())) {
                return false;
            }
        }

        return true;
    }

    public static boolean isPathPartPortable(String string) {
        return !RESERVED_WINDOWS_FILENAMES.matcher(string).matches();
    }

    public static Path createPathToResource(Path path, String string, String string2) {
        String string3 = string + string2;
        Path path2 = Paths.get(string3);
        if (path2.endsWith(string2)) {
            throw new InvalidPathException(string3, "empty resource name");
        } else {
            return path.resolve(path2);
        }
    }

    public static String getFullResourcePath(String string) {
        return FilenameUtils.getFullPath(string).replace(File.separator, "/");
    }

    public static String normalizeResourcePath(String string) {
        return FilenameUtils.normalize(string).replace(File.separator, "/");
    }

    public static DataResult<List<String>> decomposePath(String string) {
        int i = string.indexOf(47);
        if (i == -1) {
            return switch (string) {
                case "", ".", ".." -> DataResult.error(() -> "Invalid path '" + string + "'");
                default -> !isValidStrictPathSegment(string) ? DataResult.error(() -> "Invalid path '" + string + "'") : DataResult.success(List.of(string));
            };
        } else {
            List<String> list = new ArrayList();
            int j = 0;
            boolean bl = false;

            while (true) {
                String string2 = string.substring(j, i);
                switch (string2) {
                    case "":
                    case ".":
                    case "..":
                        return DataResult.error(() -> "Invalid segment '" + string2 + "' in path '" + string + "'");
                }

                if (!isValidStrictPathSegment(string2)) {
                    return DataResult.error(() -> "Invalid segment '" + string2 + "' in path '" + string + "'");
                }

                list.add(string2);
                if (bl) {
                    return DataResult.success(list);
                }

                j = i + 1;
                i = string.indexOf(47, j);
                if (i == -1) {
                    i = string.length();
                    bl = true;
                }
            }
        }
    }

    public static Path resolvePath(Path path, List<String> list) {
        int i = list.size();

        return switch (i) {
            case 0 -> path;
            case 1 -> path.resolve((String)list.get(0));
            default -> {
                String[] strings = new String[i - 1];

                for (int j = 1; j < i; j++) {
                    strings[j - 1] = (String)list.get(j);
                }

                yield path.resolve(path.getFileSystem().getPath((String)list.get(0), strings));
            }
        };
    }

    public static boolean isValidStrictPathSegment(String string) {
        return STRICT_PATH_SEGMENT_CHECK.matcher(string).matches();
    }

    public static void validatePath(String... strings) {
        if (strings.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        } else {
            for (String string : strings) {
                if (string.equals("..") || string.equals(".") || !isValidStrictPathSegment(string)) {
                    throw new IllegalArgumentException("Illegal segment " + string + " in path " + Arrays.toString(strings));
                }
            }
        }
    }

    public static void createDirectoriesSafe(Path path) throws IOException {
        Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
    }
}
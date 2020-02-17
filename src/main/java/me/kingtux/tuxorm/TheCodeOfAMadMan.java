package me.kingtux.tuxorm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class allows you to get all the files inside a Jar or inside the local jar you are current running in.
 * the goal of this is for locating resources inside the jar file.
 */
public class TheCodeOfAMadMan {
    /**
     * Get the current jar for the class
     *
     * @param clazz the class
     * @return the file for the jar
     * @throws URISyntaxException Read the toURI documentation
     */
    public static File getJarFromClass(Class clazz) throws URISyntaxException {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
    }

    /**
     * Get the Resource Files inside the Jar File
     *
     * @param file    the file
     * @param baseDir What should the files path begin with
     * @return a list of path to use in Class.getResource
     * @throws IOException if the jar file isnt correct.
     */
    public static List<String> getResourcesInJar(File file, String baseDir) throws IOException {
        return getResourcesInJar(file, baseDir, true);
    }

    /**
     * Get the Resource Files inside the Jar File
     *
     * @param file         the file
     * @param baseDir      What should the files path begin with
     * @param excludeClass Do you want to exclude Class Files.
     * @return a list of path to use in Class.getResource
     * @throws IOException if the jar file isnt correct.
     */
    public static List<String> getResourcesInJar(File file, String baseDir, boolean excludeClass) throws IOException {
        JarFile jarFile = new JarFile(file);
        List<String> contents = new ArrayList<>();
        for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
            if (jarEntry.isDirectory()) continue;
            if (jarEntry.getName().startsWith(baseDir) || baseDir.equals("/")) {
                if (excludeClass && jarEntry.getName().endsWith(".class")) continue;
                contents.add("/" + jarEntry.getName());
            }
        }
        jarFile.close();
        return contents;
    }

}




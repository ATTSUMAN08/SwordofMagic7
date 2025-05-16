package net.somrpg.swordofmagic7.utils

import net.somrpg.swordofmagic7.SomCore
import java.io.File
import java.util.jar.JarFile

/**
 * Utility class to find all classes within a package
 */
object PackageClassFinder {

    /**
     * Find all classes in a package
     *
     * @param packageName The package name to search in (e.g. "org.example.plugin.commands")
     * @return List of classes in the package
     */
    fun getClasses(packageName: String): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        val plugin = SomCore.instance

        try {
            // Get the plugin's jar file
            val pluginFile = File(plugin.javaClass.protectionDomain.codeSource.location.toURI())

            if (pluginFile.isFile) { // It's a jar file
                val jarFile = JarFile(pluginFile)

                // Replace dots with slashes for finding entries
                val packagePath = packageName.replace('.', '/')

                // Enumerate all entries in the jar
                val entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name

                    // Check if the entry is in the target package and is a .class file
                    if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                        // Convert the entry name to a class name
                        val className = entryName.substring(0, entryName.length - 6).replace('/', '.')

                        try {
                            // Load the class
                            val clazz = Class.forName(className, false, plugin.javaClass.classLoader)
                            classes.add(clazz)
                        } catch (e: ClassNotFoundException) {
                            plugin.logger.warning("Failed to load class: $className")
                            e.printStackTrace()
                        }
                    }
                }

                jarFile.close()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error loading classes from package: $packageName")
            e.printStackTrace()
        }

        return classes
    }
}
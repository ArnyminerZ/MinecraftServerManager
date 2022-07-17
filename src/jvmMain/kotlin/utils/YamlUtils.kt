package utils

import java.io.File
import java.nio.charset.Charset

fun File.parseYaml(charset: Charset = Charsets.UTF_8): Map<String, String> =
    this.readLines(charset)
        .mapNotNull { line ->
            line
                // Ignore comments
                .takeIf { !it.startsWith("#") }
                // Check that the line is correctly formatted as <key>=<value>
                ?.indexOf('=')
                ?.takeIf { it >= 0 }
                // Parse the line
                ?.let { line.substring(0, it) to line.substring(it + 1) }
        }
        .toMap()

fun File.writeYaml(yaml: Map<String, String>) =
    this.writeText(
        yaml.map { "${it.key}=${it.value}" }.joinToString("\n")
    )

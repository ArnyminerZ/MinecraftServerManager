package manager

import data.server.Server
import dev.dewy.nbt.Nbt
import dev.dewy.nbt.io.CompressionType
import dev.dewy.nbt.tags.primitive.StringTag
import mc.Datapack
import mc.VersionData
import org.json.JSONObject
import utils.newUnzip
import utils.unzip
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.text.ParseException
import java.util.UUID

object DatapackManager {
    private fun readDatapack(datapackDir: File, level: File?): Datapack {
        println("DATAPACK_MANAGER > Reading datapack contents of $datapackDir...")
        val mcMetaFile = File(datapackDir, "pack.mcmeta")
        val dataDir = File(datapackDir, "data")

        if (!mcMetaFile.exists() || !dataDir.exists())
            throw ParseException("Datapack structure is not valid", -1)

        val mcMeta = JSONObject(mcMetaFile.readText())
        val packJson = mcMeta.getJSONObject("pack")
        val packFormat = packJson.getInt("pack_format")
        val packVersion = packJson.takeIf { it.has("description") }?.takeIf { it.has("version") }?.getString("version")
        val packDescription = packJson.takeIf { it.has("description") }?.getString("description")

        val enabled = if (level != null && level.exists()) {
            val levelDat = Nbt().fromFile(level)
            val levelData = levelDat.getCompound("Data")
            val levelDatapacks = levelData.getCompound("DataPacks")
            val enabledDatapacks = levelDatapacks.getList<StringTag>("Enabled")
            val disabledDatapacks = levelDatapacks.getList<StringTag>("Disabled")

            val datapackRelativePath = datapackDir.relativeTo(datapackDir.parentFile)
            if (enabledDatapacks.contains(StringTag("file/$datapackRelativePath")))
                true
            else if (disabledDatapacks.contains(StringTag("file/$datapackRelativePath")))
                false
            else
                throw IllegalStateException("Could not find Datapack ($datapackRelativePath) in level's dat file. DataPacks=$levelDatapacks")
        } else false

        return Datapack(packFormat, packDescription, packVersion, datapackDir, enabled)
    }

    private fun isValidDatapack(datapack: Datapack, version: String, allowIncompatiblePacks: Boolean = false): Boolean {
        val packFormat = datapack.format

        // Check mcmeta
        val packCompatible = when {
            Regex("(1\\.13||1\\.14)(\\.\\d)*").matches(version) -> packFormat == 4
            Regex("(1\\.15(\\.\\d)*||1\\.16\\.[0-1])").matches(version) -> packFormat == 5
            Regex("1\\.16(\\.[2-5])*").matches(version) -> packFormat == 6
            Regex("1\\.17(\\.[0-1])*").matches(version) -> packFormat == 7
            Regex("1\\.18(\\.[0-1])*").matches(version) -> packFormat == 8
            version == "1.18.2" -> packFormat == 9
            Regex("1\\.19(\\.\\d)*").matches(version) -> packFormat == 10
            else -> false
        }
        if (!allowIncompatiblePacks && !packCompatible) {
            println("DATAPACK_MANAGER > Pack format \"$packFormat\" is not compatible with version \"$version\"")
            return false
        }

        return true
    }

    fun loadDatapack(
        file: File,
        version: VersionData,
        allowIncompatiblePacks: Boolean = false,
        deleteZipFile: Boolean = false,
        level: File? = null,
    ) = loadDatapack(file, version.id, allowIncompatiblePacks, deleteZipFile, level)

    fun loadDatapack(
        file: File,
        version: String,
        allowIncompatiblePacks: Boolean = false,
        deleteZipFile: Boolean = false,
        level: File? = null,
    ): Datapack? {
        // Check if file is a .zip
        if (!file.isFile || file.extension != "zip") {
            println("DATAPACK_MANAGER > File is not a zip ($file).")
            return null
        }

        // Decompress the ZIP file
        val tempDir = utils.createTempDir("MSC")
        println("DATAPACK_MANAGER > Decompressing $file into $tempDir...")
        file.newUnzip(tempDir)
        println("DATAPACK_MANAGER > Finished decompressing datapack.")

        // Delete the ZIP file if chosen to
        if (deleteZipFile)
            // Return null if the file could not have been deleted
            try {
                println("DATAPACK_MANAGER > Could not delete source zip file.")
                Files.delete(file.toPath())
            }catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        // Read the datapack's data
        val datapack = try {
            readDatapack(tempDir, level)
        } catch (e: ParseException) {
            println("DATAPACK_MANAGER > Datapack is not valid. Dir: $tempDir")
            return null
        }

        // Check if the datapack is valid
        if (!isValidDatapack(datapack, version, allowIncompatiblePacks)) {
            println("DATAPACK_MANAGER > Datapack is not valid.")
            return null
        }

        return datapack
    }

    fun getDatapacks(server: Server): List<Datapack> {
        val worldDir = server.worldDirectory
        val level = File(worldDir, "level.dat")
        val datapacksDir = File(worldDir, "datapacks").also { if (!it.exists()) it.mkdirs() }
        return datapacksDir
            .listFiles()
            .mapNotNull { file ->
                try {
                    file.takeIf { it.isDirectory }
                        ?.let { readDatapack(it, level) }
                        ?: file.takeIf { it.isFile && it.extension == "zip" }
                            ?.also { println("DATAPACK_MANAGER > Got datapack in zip format, loading...") }
                            ?.let { zipFile ->
                                val datapack = loadDatapack(zipFile, server.version, level = level, deleteZipFile = true)
                                    ?: return@let null
                                val newFolder = datapack
                                    .folder
                                    .let { tempDir ->
                                        println("DATAPACK_MANAGER > Copying new datapack into datapacks dir...")
                                        val target = File(datapacksDir, tempDir.name)
                                        tempDir.copyRecursively(target, true)
                                        target
                                    }
                                datapack.copy(folder = newFolder)
                            }
                            ?.also { println("DATAPACK_MANAGER > Datapack loaded, adding to server...") }
                            ?.also { addToServer(server, it) }
                } catch (e: ParseException) {
                    null
                }
            }
    }

    fun addToServer(server: Server, datapack: Datapack): Datapack {
        val worldDir = server.worldDirectory
        val datapacksDir = File(worldDir, "datapacks").also { if (!it.exists()) it.mkdirs() }
        val datapackDir = File(datapacksDir, datapack.id)

        // Move the temp folder to the server's one
        Files.move(datapack.folder.toPath(), datapackDir.toPath())

        // Delete old temp folder
        datapack.folder.deleteRecursively()

        // Toggle the datapack so it has a valid state
        return toggle(server, datapack.copy(folder = datapackDir))
    }

    fun toggle(server: Server, datapack: Datapack): Datapack {
        val levelFile = File(server.worldDirectory, "level.dat")
        val datapackTag = datapack.tag

        if (!levelFile.exists())
            return datapack
                .copy(enabled = true)
                .also { RunManager.setDatapackStatus(server, it) }

        // Read and update data
        val nbt = Nbt()
        val levelDat = nbt.fromFile(levelFile)
        val levelData = levelDat.getCompound("Data")
        val levelDatapacks = levelData.getCompound("DataPacks")
        val enabledDatapacks = levelDatapacks.getList<StringTag>("Enabled")
        val disabledDatapacks = levelDatapacks.getList<StringTag>("Disabled")
        val newEnabledStatus = if (enabledDatapacks.contains(datapackTag)) {
            disabledDatapacks.add(datapackTag)
            enabledDatapacks.remove(datapackTag)
            println("DATAPACK_MANAGER > Disabling datapack...")
            false
        } else {
            enabledDatapacks.add(datapackTag)
            if (disabledDatapacks.contains(datapackTag))
                disabledDatapacks.remove(datapackTag)
            println("DATAPACK_MANAGER > Enabling datapack...")
            true
        }

        // Write changes to file
        nbt.toFile(levelDat, levelFile, CompressionType.GZIP)

        return datapack
            .copy(enabled = newEnabledStatus)
            .also { RunManager.setDatapackStatus(server, it) }
    }
}
package net.somrpg.swordofmagic7.utils

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.block.BlockType
import java.io.FileInputStream

object SchematicUtils {
    private val cache = mutableMapOf<String, Clipboard>()

    fun deleteCache(path: String) {
        if (cache.containsKey(path)) cache.remove(path)
    }

    fun load(path: String, bypassCache: Boolean = false): Clipboard {
        if (cache.containsKey(path) && !bypassCache) {
            return cache[path]!!
        }
        val clipboard = FileInputStream(path).use { inputStream ->
            BuiltInClipboardFormat.FAST_V3.getReader(inputStream).use { reader ->
                reader.read()
            }
        }
        cache[path] = clipboard
        return clipboard
    }

    fun fromCuboid(region: CuboidRegion): BlockArrayClipboard {
        val clipboard = BlockArrayClipboard(region)

        val copy = ForwardExtentCopy(region.world, region, clipboard, region.minimumPoint)
        copy.isCopyingEntities = false
        Operations.complete(copy)
        return clipboard
    }

    fun fill(world: World, region: CuboidRegion, blockType: BlockType) {
        WorldEdit.getInstance().newEditSession(world).use { editSession ->
            editSession.setBlocks(region as Region, blockType)
        }
    }

    fun paste(schematicId: String, world: World, to: BlockVector3, ignoreAirBlocks: Boolean = false, bypassCache: Boolean = false) {
        WorldEdit.getInstance().newEditSession(world).use { editSession ->
            val clipboard = load("plugins/FastAsyncWorldEdit/schematics/${schematicId}.schem", bypassCache)
            val holder = ClipboardHolder(clipboard)
            val operation = holder.createPaste(editSession)
                .to(to)
                .ignoreAirBlocks(ignoreAirBlocks)
                .build()
            Operations.complete(operation)
            editSession.flushQueue()
        }
    }
}
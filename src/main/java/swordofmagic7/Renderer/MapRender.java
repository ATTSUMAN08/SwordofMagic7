package swordofmagic7.Renderer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import swordofmagic7.SomCore;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static swordofmagic7.Data.DataBase.DataBasePath;

public class MapRender extends MapRenderer{

    private BufferedImage image = null;
    private final CustomMapData data;

    public MapRender(CustomMapData data) {
        this.data = data;
        try {
            if (data.type == CustomMapType.Image) {
                image = ImageIO.read(new File(DataBasePath, "Image/" + data.path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(@NotNull MapView view, MapCanvas canvas, @NotNull Player player) {
        BufferedImage image = new BufferedImage(this.image.getWidth(), this.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.createGraphics();
        graphics.drawImage(this.image, data.OffsetX, data.OffsetY, null);
        canvas.drawImage(0, 0, image);
    }

    public ItemStack view(int id) {
        ItemStack myMap = new ItemStack(Material.FILLED_MAP);
        while (Bukkit.getMap(id) == null) {
            Bukkit.getServer().createMap(SomCore.world);
        }
        MapView mapView = Bukkit.getMap(id);
        mapView.getRenderers().clear();
        mapView.addRenderer(this);
        MapMeta meta = (MapMeta) myMap.getItemMeta();
        meta.setMapView(mapView);
        myMap.setItemMeta(meta);
        return myMap;
    }
}

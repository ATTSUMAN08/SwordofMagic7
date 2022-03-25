package swordofmagic7.Renderer;

public class CustomMapData {
    public CustomMapType type = CustomMapType.Image;
    public String path;
    public int OffsetX = 0;
    public int OffsetY = 0;

    public CustomMapData(CustomMapType type, String path) {
        this.type = type;
        this.path = path;
    }

    public CustomMapData(CustomMapType type, String path, int OffsetX, int OffsetY) {
        this.type = type;
        this.path = path;
        this.OffsetX = OffsetX;
        this.OffsetY = OffsetY;
    }
}

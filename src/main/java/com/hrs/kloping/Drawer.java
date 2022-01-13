package com.hrs.kloping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author github-kloping
 */
public class Drawer {
    public static String createImage(Collection<Image> images_) {
        Set<Image> images = new CopyOnWriteArraySet<>();
        images.addAll(images_);
        int width = images.size() * 50 + 75;
        int height = 200;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = bi.getGraphics();
        g.setClip(0, 0, width, height);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        int x = 0;
        for (Image image : images) {
            g.drawImage(image, x, 0, 110, height, null);
            x += 50;
        }
        g.dispose();
        String name = UUID.randomUUID() + ".jpg";
        new File("./temp").mkdirs();
        File file = new File("./temp/" + name);
        try {
            ImageIO.write(bi, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private static final Map<String, Image> map = new ConcurrentHashMap<>();

    public static final synchronized Image loadImage(String fileName) {
        try {
            if (map.containsKey(fileName)) return map.get(fileName);
            BufferedImage img = ImageIO.read(new File(fileName));
            map.put(fileName, img);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static final synchronized Image loadImage(URL url) {
        try {
            if (map.containsKey(url.getPath())) return map.get(url.getPath());
            BufferedImage img = ImageIO.read(url);
            map.put(url.getPath(), img);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}

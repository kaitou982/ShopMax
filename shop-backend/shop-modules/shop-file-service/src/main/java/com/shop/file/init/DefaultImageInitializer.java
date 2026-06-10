package com.shop.file.init;

import com.shop.common.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认占位图初始化 — 首次启动时向 MinIO 上传默认图片
 *
 * @author shop
 * @since 2026-05-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultImageInitializer implements CommandLineRunner {

    private final StorageService storageService;

    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color FG_COLOR = new Color(189, 189, 189);
    private static final Color ACCENT_COLOR = new Color(255, 80, 0);

    /** 类型 → (宽, 高, 描述文字) */
    private static final Map<String, ImageDef> DEFAULTS = new LinkedHashMap<>();

    static {
        DEFAULTS.put("product", new ImageDef(400, 400, "商品图片"));
        DEFAULTS.put("avatar", new ImageDef(200, 200, "头像"));
        DEFAULTS.put("brand", new ImageDef(200, 200, "品牌"));
        DEFAULTS.put("category", new ImageDef(200, 200, "分类"));
        DEFAULTS.put("cover", new ImageDef(750, 422, "封面"));
        DEFAULTS.put("community", new ImageDef(400, 400, "社区"));
    }

    @Override
    public void run(String... args) {
        log.info("检查并初始化默认占位图...");
        for (Map.Entry<String, ImageDef> entry : DEFAULTS.entrySet()) {
            String type = entry.getKey();
            String objectName = "defaults/" + type + ".png";
            try {
                if (storageService.exists(objectName)) {
                    log.info("默认占位图已存在: {}", objectName);
                    continue;
                }
                ImageDef def = entry.getValue();
                byte[] imageBytes = generateImage(def.width, def.height, def.label);
                try (InputStream stream = new ByteArrayInputStream(imageBytes)) {
                    String url = storageService.upload(objectName, stream, "image/png", imageBytes.length);
                    log.info("默认占位图创建成功: {} → {}", objectName, url);
                }
            } catch (Exception e) {
                log.warn("默认占位图创建失败 (MinIO 可能未启动): {} — {}", objectName, e.getMessage());
            }
        }
    }

    private byte[] generateImage(int width, int height, String label) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 背景
        g.setColor(BG_COLOR);
        g.fillRoundRect(0, 0, width, height, 16, 16);

        // 图标
        int iconSize = (int) (Math.min(width, height) * 0.3);
        int cx = width / 2;
        int cy = (int) (height * 0.38);
        drawIcon(g, label, cx, cy, iconSize);

        // 文字
        g.setColor(FG_COLOR);
        int fontSize = (int) (Math.min(width, height) * 0.09);
        Font font = new Font("Microsoft YaHei", Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        g.drawString(label, (width - textWidth) / 2f, (float) (height * 0.72));

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private void drawIcon(Graphics2D g, String label, int cx, int cy, int size) {
        g.setColor(ACCENT_COLOR);
        int half = size / 2;

        switch (label) {
            case "商品图片" -> {
                RoundRectangle2D box = new RoundRectangle2D.Float(cx - half, cy - half, size, size, size / 6f, size / 6f);
                g.fill(box);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(size * 0.08f));
                int tickLen = size / 4;
                g.drawLine(cx - tickLen, cy, cx, cy + tickLen);
                g.drawLine(cx, cy + tickLen, cx + tickLen, cy - tickLen / 2);
            }
            case "头像" -> {
                Ellipse2D circle = new Ellipse2D.Float(cx - half, cy - half, size, size);
                g.fill(circle);
                g.setColor(Color.WHITE);
                int headR = size / 5;
                g.fillOval(cx - headR, cy - size / 5, headR * 2, headR * 2);
                int bodyW = size / 2;
                g.fillOval(cx - bodyW / 2, cy + size / 6, bodyW, bodyW);
            }
            case "品牌" -> {
                g.setStroke(new BasicStroke(size * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int q = size / 4;
                int[] xs = {cx, cx + q, cx, cx - q};
                int[] ys = {cy - q, cy, cy + q, cy};
                g.fillPolygon(xs, ys, 4);
            }
            case "分类" -> {
                int gap = size / 10;
                int cell = (size - gap) / 2;
                int arc = size / 8;
                g.fillRoundRect(cx - half, cy - half, cell, cell, arc, arc);
                g.fillRoundRect(cx - half + cell + gap, cy - half, cell, cell, arc, arc);
                g.fillRoundRect(cx - half, cy - half + cell + gap, cell, cell, arc, arc);
                g.fillRoundRect(cx - half + cell + gap, cy - half + cell + gap, cell, cell, arc, arc);
            }
            case "封面" -> {
                RoundRectangle2D rect = new RoundRectangle2D.Float(cx - size / 2, cy - half, size, size, size / 8f, size / 8f);
                g.fill(rect);
                g.setColor(Color.WHITE);
                int triSize = size / 4;
                int tx = cx + triSize / 4;
                int[] triX = {tx, tx + triSize, tx};
                int[] triY = {cy - triSize / 2, cy, cy + triSize / 2};
                g.fillPolygon(triX, triY, 3);
            }
            case "社区" -> {
                RoundRectangle2D rect = new RoundRectangle2D.Float(cx - size / 2, cy - half, size, size, size / 8f, size / 8f);
                g.fill(rect);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(size * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int bubbleR = size / 5;
                g.fillOval(cx - bubbleR, cy - bubbleR / 2, bubbleR * 2, bubbleR * 2);
                int tailH = size / 6;
                int[] tailX = {cx + bubbleR / 3, cx + bubbleR, cx + bubbleR / 2};
                int[] tailY = {cy + bubbleR, cy + bubbleR, cy + bubbleR + tailH};
                g.fillPolygon(tailX, tailY, 3);
            }
            default -> {
                g.fillOval(cx - half, cy - half, size, size);
            }
        }
    }

    private record ImageDef(int width, int height, String label) {
    }
}

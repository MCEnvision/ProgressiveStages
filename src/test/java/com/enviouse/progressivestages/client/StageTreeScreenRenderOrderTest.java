package com.enviouse.progressivestages.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageTreeScreenRenderOrderTest {

    private static final Path PROJECT = Path.of(System.getProperty("progressivestages.projectDir"));
    private static final Path SCREEN = PROJECT.resolve(
        "src/main/java/com/enviouse/progressivestages/client/gui/StageTreeScreen.java");

    @Test
    void blurPassRunsOnceBeforeTheMapAndWidgets() throws IOException {
        String source = Files.readString(SCREEN);
        int start = source.indexOf("public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick)");
        int end = source.indexOf("private void renderMapBackground", start);
        assertTrue(start >= 0 && end > start);

        String renderMethod = source.substring(start, end);
        int background = renderMethod.indexOf("renderBackground(");
        int map = renderMethod.indexOf("renderMapBackground(");
        int frame = renderMethod.indexOf("renderWindowFrame(");
        int widgets = renderMethod.indexOf("for (var renderable : renderables)");

        assertEquals(1, occurrences(renderMethod, "renderBackground("));
        assertFalse(renderMethod.contains("super.render("));
        assertTrue(background < map);
        assertTrue(frame < widgets);
        assertTrue(renderMethod.contains("g.pose().translate(0.0F, 0.0F, 200.0F)"));
    }

    @Test
    void mapDragCanBeginOnAStageNode() throws IOException {
        String source = Files.readString(SCREEN);

        assertTrue(source.contains("pressedNode = nodeAt(mouseX, mouseY)"));
        assertTrue(source.contains("dragDistance += Math.hypot(dragX, dragY)"));
        assertTrue(source.contains("StageTreeViewport.drag("));
        assertTrue(source.contains("if (dragDistance < 2.0 && pressedNode != null)"));
    }

    @Test
    void mapWheelUsesTheProgressiveSkillsCursorZoomModel() throws IOException {
        String source = Files.readString(SCREEN);

        assertTrue(source.contains("StageTreeViewport.zoomAt("));
        assertTrue(source.contains("mouseX - (mapLeft + mapRight) / 2.0D"));
        assertTrue(source.contains("mouseY - (mapTop + mapBottom) / 2.0D"));
        assertTrue(source.contains("zoom = camera.zoom()"));
    }

    @Test
    void inspectorUsesStructuredReadableModifierCards() throws IOException {
        String source = Files.readString(SCREEN);

        assertTrue(source.contains("renderModifierPreview(g, modifier"));
        assertTrue(source.contains("humanizeIdentifier(slotGroup)"));
        assertTrue(source.contains("selectorIconCache.computeIfAbsent"));
        assertTrue(source.contains("BuiltInRegistries.BLOCK.getTag"));
        assertTrue(source.contains("BuiltInRegistries.ITEM.getTag"));
        assertFalse(source.contains("for (String modifier : data.modifiers())"));
    }

    @Test
    void progressionWindowUsesTheCompactAdvancementFrame() throws IOException {
        String source = Files.readString(SCREEN);

        assertTrue(source.contains("Math.min(width - 24, 460)"));
        assertTrue(source.contains("Math.min(height - 38, 250)"));
        assertTrue(source.contains("Math.min(x + width, lineX + 42)"));
    }

    @Test
    void categoryMenuRendersAboveStageItemDepth() throws IOException {
        String source = Files.readString(SCREEN);
        int start = source.indexOf("private void renderCategoryMenu(GuiGraphics g, int mouseX, int mouseY)");
        int end = source.indexOf("private void tileHorizontal", start);
        assertTrue(start >= 0 && end > start);

        String categoryMenu = source.substring(start, end);
        int push = categoryMenu.indexOf("g.pose().pushPose()");
        int foreground = categoryMenu.indexOf("g.pose().translate(0.0F, 0.0F, 1200.0F)");
        int panel = categoryMenu.indexOf("fillPixelRounded(g, categoryMenuX, categoryMenuY");
        int pop = categoryMenu.indexOf("g.pose().popPose()");

        assertTrue(push >= 0);
        assertTrue(push < foreground);
        assertTrue(foreground < panel);
        assertTrue(panel < pop);
        assertEquals(1, occurrences(categoryMenu, "g.pose().pushPose()"));
        assertEquals(1, occurrences(categoryMenu, "g.pose().popPose()"));
    }

    @Test
    void openCategoryOverlayConsumesMapClickAndScrollInput() throws IOException {
        String source = Files.readString(SCREEN);
        int clickStart = source.indexOf("public boolean mouseClicked(double mouseX, double mouseY, int button)");
        int clickEnd = source.indexOf("public boolean mouseDragged", clickStart);
        int scrollStart = source.indexOf("public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)");
        int scrollEnd = source.indexOf("public boolean keyPressed", scrollStart);

        String click = source.substring(clickStart, clickEnd);
        String scroll = source.substring(scrollStart, scrollEnd);
        assertTrue(click.contains("categoryOpen = false;\n                return true;"));
        assertTrue(scroll.contains("if (categoryOpen) {"));
        assertTrue(scroll.contains("return true;"));
    }

    private static int occurrences(String value, String target) {
        int count = 0;
        for (int index = value.indexOf(target); index >= 0; index = value.indexOf(target, index + target.length())) {
            count++;
        }
        return count;
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 */
package com.swiftclient.gui;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.gui.Sprites;
import com.swiftclient.hud.ArmorStatusModule;
import com.swiftclient.hud.ClockModule;
import com.swiftclient.hud.CoordinatesModule;
import com.swiftclient.hud.CpsModule;
import com.swiftclient.hud.FPSModule;
import com.swiftclient.hud.KeystrokesModule;
import com.swiftclient.hud.PotionEffectsModule;
import com.swiftclient.modules.Module;
import com.swiftclient.performance.DynamicRenderDistanceModule;
import com.swiftclient.performance.MemoryModule;
import com.swiftclient.performance.ParticleDensityModule;
import com.swiftclient.player.FovModule;
import com.swiftclient.player.ToggleSneakModule;
import com.swiftclient.player.ToggleSprintModule;
import com.swiftclient.render.BlockOutlineModule;
import com.swiftclient.render.ChunkBordersModule;
import com.swiftclient.render.CrosshairModule;
import com.swiftclient.render.FreelookModule;
import com.swiftclient.render.FullbrightModule;
import com.swiftclient.render.GuiScaleModule;
import com.swiftclient.render.HitboxesModule;
import com.swiftclient.render.LightLevelModule;
import com.swiftclient.render.ZoomModule;
import com.swiftclient.restyle.ActionBarModule;
import com.swiftclient.restyle.BossbarModule;
import com.swiftclient.restyle.ScoreboardModule;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5348;

public final class SwiftModuleSettings
extends class_437 {
    private static final int ROW_TOP = 66;
    private static final int ROW_H = 30;
    private final Module module;
    private final class_437 parent;
    private final List<Row> rows = new ArrayList<Row>();
    private int dragging = -1;

    public SwiftModuleSettings(Module module, class_437 parent) {
        super((class_2561)class_2561.method_43470((String)(module.name() + " settings")));
        this.module = module;
        this.parent = parent;
    }

    public boolean method_25421() {
        return false;
    }

    protected void method_25426() {
        this.rows.clear();
        this.dragging = -1;
        String n = this.module.name();
        this.rows.add(new ToggleRow("Enabled", this.module::enabled, this.module::setEnabled));
        if (this.module.hasPosition()) {
            int xMax = Math.max(160, this.field_22789 - 110);
            int yMax = Math.max(120, this.field_22790 - 60);
            this.rows.add(new SliderRow("Position X", 0, xMax, () -> ClientConfig.getX(n, this.module.defaultX()), v -> ClientConfig.setPosSilent(n, v, ClientConfig.getY(n, this.module.defaultY()))));
            this.rows.add(new SliderRow("Position Y", 0, yMax, () -> ClientConfig.getY(n, this.module.defaultY()), v -> ClientConfig.setPosSilent(n, ClientConfig.getX(n, this.module.defaultX()), v)));
        }
        if (this.module.hasBackground()) {
            this.rows.add(new ToggleRow("Background", () -> ClientConfig.getBool(n, "background", true), v -> ClientConfig.setBool(n, "background", v)));
            this.rows.add(new SliderRow("Opacity", 20, 255, () -> ClientConfig.getInt(n, "opacity", 255), v -> ClientConfig.setIntSilent(n, "opacity", v)));
        }
        if (this.module instanceof FPSModule) {
            this.rows.add(new ToggleRow("Background", () -> ClientConfig.getBool(n, "background", true), v -> ClientConfig.setBool(n, "background", v)));
        } else if (this.module instanceof CoordinatesModule) {
            this.rows.add(new ToggleRow("Axis labels", () -> ClientConfig.getBool(n, "axis", true), v -> ClientConfig.setBool(n, "axis", v)));
        } else if (this.module instanceof KeystrokesModule) {
            this.rows.add(new ToggleRow("Mouse buttons", () -> ClientConfig.getBool(n, "mouse", true), v -> ClientConfig.setBool(n, "mouse", v)));
        } else if (this.module instanceof PotionEffectsModule) {
            this.rows.add(new ToggleRow("Durations", () -> ClientConfig.getBool(n, "duration", true), v -> ClientConfig.setBool(n, "duration", v)));
        } else if (this.module instanceof CpsModule) {
            this.rows.add(new ToggleRow("Right click", () -> ClientConfig.getBool(n, "right", true), v -> ClientConfig.setBool(n, "right", v)));
        } else if (this.module instanceof ClockModule) {
            this.rows.add(new ToggleRow("Seconds", () -> ClientConfig.getBool(n, "seconds", false), v -> ClientConfig.setBool(n, "seconds", v)));
        } else if (this.module instanceof ArmorStatusModule) {
            this.rows.add(new ToggleRow("Durability", () -> ClientConfig.getBool(n, "durability", true), v -> ClientConfig.setBool(n, "durability", v)));
        } else if (this.module instanceof ZoomModule) {
            this.rows.add(new SliderRow("Zoom strength", 15, 60, () -> ClientConfig.getInt(n, "strength", 30), v -> ClientConfig.setIntSilent(n, "strength", v)));
            this.rows.add(new NoteRow("Hold C to zoom."));
        } else if (this.module instanceof FreelookModule) {
            this.rows.add(new NoteRow("Look around freely while enabled."));
        } else if (this.module instanceof CrosshairModule) {
            this.rows.add(new ToggleRow("Center dot", () -> ClientConfig.getBool(n, "dot", false), v -> ClientConfig.setBool(n, "dot", v)));
        } else if (this.module instanceof GuiScaleModule) {
            this.rows.add(new SliderRow("Scale", 0, 3, () -> ClientConfig.getInt(n, "scale", 2), v -> ClientConfig.setIntSilent(n, "scale", v)));
        } else if (this.module instanceof BlockOutlineModule) {
            this.rows.add(new NoteRow("Outline draws in your accent color."));
        } else if (this.module instanceof ChunkBordersModule) {
            this.rows.add(new SliderRow("Radius", 1, 4, () -> ClientConfig.getInt(n, "radius", 2), v -> ClientConfig.setIntSilent(n, "radius", v)));
        } else if (this.module instanceof HitboxesModule) {
            this.rows.add(new NoteRow("Boxes draw around nearby entities."));
        } else if (this.module instanceof ToggleSneakModule) {
            this.rows.add(new NoteRow("Forces sneak held while enabled."));
        } else if (this.module instanceof ToggleSprintModule) {
            this.rows.add(new NoteRow("Forces sprint held while enabled."));
        } else if (this.module instanceof FovModule) {
            this.rows.add(new SliderRow("Field of view", 30, 120, () -> ClientConfig.getInt(n, "fov", 90), v -> ClientConfig.setIntSilent(n, "fov", v)));
        } else if (this.module instanceof ParticleDensityModule) {
            this.rows.add(new SliderRow("Amount", 0, 2, () -> ClientConfig.getInt(n, "amount", 1), v -> ClientConfig.setIntSilent(n, "amount", v)));
        } else if (this.module instanceof FullbrightModule) {
            this.rows.add(new SliderRow("Strength", 1, 10, () -> ClientConfig.getInt(n, "strength", 10), v -> ClientConfig.setIntSilent(n, "strength", v)));
        } else if (this.module instanceof DynamicRenderDistanceModule) {
            this.rows.add(new SliderRow("Target FPS", 30, 120, () -> ClientConfig.getInt(n, "target", 60), v -> ClientConfig.setIntSilent(n, "target", v)));
            this.rows.add(new SliderRow("Min chunks", 2, 8, () -> ClientConfig.getInt(n, "min", 2), v -> ClientConfig.setIntSilent(n, "min", v)));
            this.rows.add(new SliderRow("Max chunks", 8, 32, () -> ClientConfig.getInt(n, "max", 32), v -> ClientConfig.setIntSilent(n, "max", v)));
        } else if (this.module instanceof ActionBarModule || this.module instanceof BossbarModule || this.module instanceof ScoreboardModule) {
            this.rows.add(new NoteRow("Replaces the vanilla element."));
        } else if (this.module instanceof LightLevelModule) {
            this.rows.add(new NoteRow("Block-light readout at your feet."));
        } else if (this.module instanceof MemoryModule) {
            this.rows.add(new NoteRow("Live Java heap usage."));
        }
    }

    public void method_25419() {
        ClientConfig.save();
        this.field_22787.method_1507(this.parent);
    }

    private int trackX0() {
        return 220;
    }

    private int trackX1() {
        return Math.max(this.trackX0() + 60, this.field_22789 - 110);
    }

    private int rowY(int i) {
        return 66 + i * 30;
    }

    private boolean inBack(int mx, int my) {
        int w = this.field_22793.method_27525((class_5348)SwiftText.of("< BACK")) + 20;
        return mx >= this.field_22789 - 24 - w && mx < this.field_22789 - 24 && my >= 14 && my < 36;
    }

    private boolean inReset(int mx, int my) {
        if (!this.module.hasPosition()) {
            return false;
        }
        int w = this.field_22793.method_27525((class_5348)SwiftText.of("RESET POSITION")) + 24;
        return mx >= 24 && mx < 24 + w && my >= this.field_22790 - 40 && my < this.field_22790 - 18;
    }

    private boolean inToggle(int i, int mx, int my) {
        int y = this.rowY(i);
        return mx >= this.field_22789 - 24 - 52 && mx < this.field_22789 - 24 && my >= y + 4 && my < y + 26;
    }

    private boolean inSlider(int i, int mx, int my) {
        int y = this.rowY(i);
        return mx >= this.trackX0() - 8 && mx < this.trackX1() + 8 && my >= y && my < y + 30;
    }

    private int sliderVal(int i, int mx) {
        SliderRow s = (SliderRow)this.rows.get(i);
        int w = this.trackX1() - this.trackX0();
        float t = (float)(mx - this.trackX0() - 5) / (float)Math.max(1, w - 10);
        t = Math.max(0.0f, Math.min(1.0f, t));
        return s.min() + Math.round(t * (float)(s.max() - s.min()));
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        int mx = (int)click.comp_4798();
        int my = (int)click.comp_4799();
        if (click.method_74245() == 0 && this.inBack(mx, my)) {
            this.method_25419();
            return true;
        }
        if (click.method_74245() == 0 && this.inReset(mx, my)) {
            ClientConfig.setPos(this.module.name(), this.module.defaultX(), this.module.defaultY());
            return true;
        }
        if (click.method_74245() != 0) {
            return false;
        }
        for (int i = 0; i < this.rows.size(); ++i) {
            Row r = this.rows.get(i);
            if (r instanceof ToggleRow) {
                ToggleRow t = (ToggleRow)r;
                if (this.inToggle(i, mx, my)) {
                    t.set().accept(!t.get().getAsBoolean());
                    return true;
                }
            }
            if (!(r instanceof SliderRow) || !this.inSlider(i, mx, my)) continue;
            ((SliderRow)r).set().accept(this.sliderVal(i, mx));
            this.dragging = i;
            return true;
        }
        return false;
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (this.dragging < 0 || this.dragging >= this.rows.size()) {
            return false;
        }
        Row row = this.rows.get(this.dragging);
        if (row instanceof SliderRow) {
            SliderRow s = (SliderRow)row;
            s.set().accept(this.sliderVal(this.dragging, (int)click.comp_4798()));
            return true;
        }
        return false;
    }

    public boolean method_25406(class_11909 click) {
        this.dragging = -1;
        return false;
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        c.method_25294(0, 0, this.field_22789, this.field_22790, 0x33000000);
        Ui.spacedText(c, this.field_22793, this.module.name().toUpperCase(), 24 + this.field_22793.method_1727(this.module.name().toUpperCase()) / 2, 22, -855305, 2);
        c.method_27535(this.field_22793, SwiftText.of(this.module.description()), 24, 36, -7697776);
        int bw = this.field_22793.method_27525((class_5348)SwiftText.of("< BACK")) + 20;
        boolean hBack = this.inBack(mouseX, mouseY);
        Ui.sprite(c, hBack ? Sprites.BTN_HOVER : Sprites.BTN, this.field_22789 - 24 - bw, 14, bw, 22);
        c.method_27534(this.field_22793, SwiftText.of("< BACK"), this.field_22789 - 24 - bw / 2, 21, -855305);
        for (int i = 0; i < this.rows.size(); ++i) {
            Row r = this.rows.get(i);
            int y = this.rowY(i);
            Ui.sprite(c, Sprites.OPT, 16, y - 2, this.field_22789 - 32, 26);
            if (r instanceof ToggleRow) {
                ToggleRow t = (ToggleRow)r;
                c.method_27535(this.field_22793, SwiftText.of(t.label()), 28, y + 9, -855305);
                Ui.toggle(c, this.field_22789 - 28 - 52, y + 4, 52, 22, t.get().getAsBoolean());
                continue;
            }
            if (r instanceof SliderRow) {
                SliderRow s = (SliderRow)r;
                int v = s.get().getAsInt();
                c.method_27535(this.field_22793, SwiftText.of(s.label()), 28, y + 9, -855305);
                c.method_27535(this.field_22793, SwiftText.of(String.valueOf(v)), this.field_22789 - 28 - this.field_22793.method_27525((class_5348)SwiftText.of(String.valueOf(v))), y + 9, -4605239);
                Ui.slider(c, this.trackX0(), y, this.trackX1() - this.trackX0(), v, s.min(), s.max());
                continue;
            }
            c.method_27535(this.field_22793, SwiftText.of(r.label()), 28, y + 9, -7697776);
        }
        if (this.module.hasPosition()) {
            int rw = this.field_22793.method_27525((class_5348)SwiftText.of("RESET POSITION")) + 24;
            boolean hR = this.inReset(mouseX, mouseY);
            Ui.sprite(c, hR ? Sprites.BTN_HOVER : Sprites.BTN, 24, this.field_22790 - 40, rw, 22);
            c.method_27534(this.field_22793, SwiftText.of("RESET POSITION"), 24 + rw / 2, this.field_22790 - 33, -855305);
        }
        String hint = "Drag the sliders \u2014 your HUD updates live behind this menu";
        c.method_27535(this.field_22793, SwiftText.of(hint), this.field_22789 - this.field_22793.method_27525((class_5348)SwiftText.of(hint)) - 24, this.field_22790 - 26, -7697776);
        super.method_25394(c, mouseX, mouseY, delta);
    }

    private record ToggleRow(String label, BooleanSupplier get, Consumer<Boolean> set) implements Row
    {
    }

    private record SliderRow(String label, int min, int max, IntSupplier get, IntConsumer set) implements Row
    {
    }

    private record NoteRow(String label) implements Row
    {
    }

    private static interface Row {
        public String label();
    }
}


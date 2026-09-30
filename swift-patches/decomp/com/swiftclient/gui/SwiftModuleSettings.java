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

    public SwiftModuleSettings(Module module, class_437 class_4372) {
        super((class_2561)class_2561.method_43470((String)(module.name() + " settings")));
        this.module = module;
        this.parent = class_4372;
    }

    public boolean method_25421() {
        return false;
    }

    protected void method_25426() {
        this.rows.clear();
        this.dragging = -1;
        String string = this.module.name();
        this.rows.add(new ToggleRow("Enabled", this.module::enabled, this.module::setEnabled));
        if (this.module.hasPosition()) {
            int n2 = Math.max(160, this.field_22789 - 110);
            int n3 = Math.max(120, this.field_22790 - 60);
            this.rows.add(new SliderRow("Position X", 0, n2, () -> ClientConfig.getX(string, this.module.defaultX()), n -> ClientConfig.setPosSilent(string, n, ClientConfig.getY(string, this.module.defaultY()))));
            this.rows.add(new SliderRow("Position Y", 0, n3, () -> ClientConfig.getY(string, this.module.defaultY()), n -> ClientConfig.setPosSilent(string, ClientConfig.getX(string, this.module.defaultX()), n)));
        }
        if (this.module.hasBackground()) {
            this.rows.add(new ToggleRow("Background", () -> ClientConfig.getBool(string, "background", true), bl -> ClientConfig.setBool(string, "background", bl)));
            this.rows.add(new SliderRow("Opacity", 20, 255, () -> ClientConfig.getInt(string, "opacity", 255), n -> ClientConfig.setIntSilent(string, "opacity", n)));
        }
        if (this.module instanceof FPSModule) {
            this.rows.add(new ToggleRow("Background", () -> ClientConfig.getBool(string, "background", true), bl -> ClientConfig.setBool(string, "background", bl)));
        } else if (this.module instanceof CoordinatesModule) {
            this.rows.add(new ToggleRow("Axis labels", () -> ClientConfig.getBool(string, "axis", true), bl -> ClientConfig.setBool(string, "axis", bl)));
        } else if (this.module instanceof KeystrokesModule) {
            this.rows.add(new ToggleRow("Mouse buttons", () -> ClientConfig.getBool(string, "mouse", true), bl -> ClientConfig.setBool(string, "mouse", bl)));
        } else if (this.module instanceof PotionEffectsModule) {
            this.rows.add(new ToggleRow("Durations", () -> ClientConfig.getBool(string, "duration", true), bl -> ClientConfig.setBool(string, "duration", bl)));
        } else if (this.module instanceof CpsModule) {
            this.rows.add(new ToggleRow("Right click", () -> ClientConfig.getBool(string, "right", true), bl -> ClientConfig.setBool(string, "right", bl)));
        } else if (this.module instanceof ClockModule) {
            this.rows.add(new ToggleRow("Seconds", () -> ClientConfig.getBool(string, "seconds", false), bl -> ClientConfig.setBool(string, "seconds", bl)));
        } else if (this.module instanceof ArmorStatusModule) {
            this.rows.add(new ToggleRow("Durability", () -> ClientConfig.getBool(string, "durability", true), bl -> ClientConfig.setBool(string, "durability", bl)));
        } else if (this.module instanceof ZoomModule) {
            this.rows.add(new SliderRow("Zoom strength", 15, 60, () -> ClientConfig.getInt(string, "strength", 30), n -> ClientConfig.setIntSilent(string, "strength", n)));
            this.rows.add(new NoteRow("Hold C to zoom."));
        } else if (this.module instanceof FreelookModule) {
            this.rows.add(new NoteRow("Look around freely while enabled."));
        } else if (this.module instanceof CrosshairModule) {
            this.rows.add(new ToggleRow("Center dot", () -> ClientConfig.getBool(string, "dot", false), bl -> ClientConfig.setBool(string, "dot", bl)));
        } else if (this.module instanceof GuiScaleModule) {
            this.rows.add(new SliderRow("Scale", 0, 3, () -> ClientConfig.getInt(string, "scale", 2), n -> ClientConfig.setIntSilent(string, "scale", n)));
        } else if (this.module instanceof BlockOutlineModule) {
            this.rows.add(new NoteRow("Outline draws in your accent color."));
        } else if (this.module instanceof ChunkBordersModule) {
            this.rows.add(new SliderRow("Radius", 1, 4, () -> ClientConfig.getInt(string, "radius", 2), n -> ClientConfig.setIntSilent(string, "radius", n)));
        } else if (this.module instanceof HitboxesModule) {
            this.rows.add(new NoteRow("Boxes draw around nearby entities."));
        } else if (this.module instanceof ToggleSneakModule) {
            this.rows.add(new NoteRow("Forces sneak held while enabled."));
        } else if (this.module instanceof ToggleSprintModule) {
            this.rows.add(new NoteRow("Forces sprint held while enabled."));
        } else if (this.module instanceof FovModule) {
            this.rows.add(new SliderRow("Field of view", 30, 120, () -> ClientConfig.getInt(string, "fov", 90), n -> ClientConfig.setIntSilent(string, "fov", n)));
        } else if (this.module instanceof ParticleDensityModule) {
            this.rows.add(new SliderRow("Amount", 0, 2, () -> ClientConfig.getInt(string, "amount", 1), n -> ClientConfig.setIntSilent(string, "amount", n)));
        } else if (this.module instanceof FullbrightModule) {
            this.rows.add(new SliderRow("Strength", 1, 10, () -> ClientConfig.getInt(string, "strength", 10), n -> ClientConfig.setIntSilent(string, "strength", n)));
        } else if (this.module instanceof DynamicRenderDistanceModule) {
            this.rows.add(new SliderRow("Target FPS", 30, 120, () -> ClientConfig.getInt(string, "target", 60), n -> ClientConfig.setIntSilent(string, "target", n)));
            this.rows.add(new SliderRow("Min chunks", 2, 8, () -> ClientConfig.getInt(string, "min", 2), n -> ClientConfig.setIntSilent(string, "min", n)));
            this.rows.add(new SliderRow("Max chunks", 8, 32, () -> ClientConfig.getInt(string, "max", 32), n -> ClientConfig.setIntSilent(string, "max", n)));
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

    private int rowY(int n) {
        return 66 + n * 30;
    }

    private boolean inBack(int n, int n2) {
        int n3 = this.field_22793.method_27525((class_5348)SwiftText.of("< BACK")) + 20;
        return n >= this.field_22789 - 24 - n3 && n < this.field_22789 - 24 && n2 >= 14 && n2 < 36;
    }

    private boolean inReset(int n, int n2) {
        if (!this.module.hasPosition()) {
            return false;
        }
        int n3 = this.field_22793.method_27525((class_5348)SwiftText.of("RESET POSITION")) + 24;
        return n >= 24 && n < 24 + n3 && n2 >= this.field_22790 - 40 && n2 < this.field_22790 - 18;
    }

    private boolean inToggle(int n, int n2, int n3) {
        int n4 = this.rowY(n);
        return n2 >= this.field_22789 - 24 - 52 && n2 < this.field_22789 - 24 && n3 >= n4 + 4 && n3 < n4 + 26;
    }

    private boolean inSlider(int n, int n2, int n3) {
        int n4 = this.rowY(n);
        return n2 >= this.trackX0() - 8 && n2 < this.trackX1() + 8 && n3 >= n4 && n3 < n4 + 30;
    }

    private int sliderVal(int n, int n2) {
        SliderRow sliderRow = (SliderRow)this.rows.get(n);
        int n3 = this.trackX1() - this.trackX0();
        float f = (float)(n2 - this.trackX0() - 5) / (float)Math.max(1, n3 - 10);
        f = Math.max(0.0f, Math.min(1.0f, f));
        return sliderRow.min() + Math.round(f * (float)(sliderRow.max() - sliderRow.min()));
    }

    public boolean method_25402(class_11909 class_119092, boolean bl) {
        int n = (int)class_119092.comp_4798();
        int n2 = (int)class_119092.comp_4799();
        if (class_119092.method_74245() == 0 && this.inBack(n, n2)) {
            this.method_25419();
            return true;
        }
        if (class_119092.method_74245() == 0 && this.inReset(n, n2)) {
            ClientConfig.setPos(this.module.name(), this.module.defaultX(), this.module.defaultY());
            return true;
        }
        if (class_119092.method_74245() != 0) {
            return false;
        }
        for (int i = 0; i < this.rows.size(); ++i) {
            Row row = this.rows.get(i);
            if (row instanceof ToggleRow) {
                ToggleRow toggleRow = (ToggleRow)row;
                if (this.inToggle(i, n, n2)) {
                    toggleRow.set().accept(!toggleRow.get().getAsBoolean());
                    return true;
                }
            }
            if (!(row instanceof SliderRow) || !this.inSlider(i, n, n2)) continue;
            ((SliderRow)row).set().accept(this.sliderVal(i, n));
            this.dragging = i;
            return true;
        }
        return false;
    }

    public boolean method_25403(class_11909 class_119092, double d, double d2) {
        if (this.dragging < 0 || this.dragging >= this.rows.size()) {
            return false;
        }
        Row row = this.rows.get(this.dragging);
        if (row instanceof SliderRow) {
            SliderRow sliderRow = (SliderRow)row;
            sliderRow.set().accept(this.sliderVal(this.dragging, (int)class_119092.comp_4798()));
            return true;
        }
        return false;
    }

    public boolean method_25406(class_11909 class_119092) {
        this.dragging = -1;
        return false;
    }

    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        int n3;
        class_3322.method_25294(0, 0, this.field_22789, this.field_22790, 0x33000000);
        Ui.spacedText(class_3322, this.field_22793, this.module.name().toUpperCase(), 24 + this.field_22793.method_1727(this.module.name().toUpperCase()) / 2, 22, -855305, 2);
        class_3322.method_27535(this.field_22793, SwiftText.of(this.module.description()), 24, 36, -7697776);
        int n4 = this.field_22793.method_27525((class_5348)SwiftText.of("< BACK")) + 20;
        boolean bl = this.inBack(n, n2);
        Ui.sprite(class_3322, bl ? Sprites.BTN_HOVER : Sprites.BTN, this.field_22789 - 24 - n4, 14, n4, 22);
        class_3322.method_27534(this.field_22793, SwiftText.of("< BACK"), this.field_22789 - 24 - n4 / 2, 21, -855305);
        for (n3 = 0; n3 < this.rows.size(); ++n3) {
            Record record;
            Row row = this.rows.get(n3);
            int n5 = this.rowY(n3);
            Ui.sprite(class_3322, Sprites.OPT, 16, n5 - 2, this.field_22789 - 32, 26);
            if (row instanceof ToggleRow) {
                record = (ToggleRow)row;
                class_3322.method_27535(this.field_22793, SwiftText.of(((ToggleRow)record).label()), 28, n5 + 9, -855305);
                Ui.toggle(class_3322, this.field_22789 - 28 - 52, n5 + 4, 52, 22, ((ToggleRow)record).get().getAsBoolean());
                continue;
            }
            if (row instanceof SliderRow) {
                record = (SliderRow)row;
                int n6 = ((SliderRow)record).get().getAsInt();
                class_3322.method_27535(this.field_22793, SwiftText.of(((SliderRow)record).label()), 28, n5 + 9, -855305);
                class_3322.method_27535(this.field_22793, SwiftText.of(String.valueOf(n6)), this.field_22789 - 28 - this.field_22793.method_27525((class_5348)SwiftText.of(String.valueOf(n6))), n5 + 9, -4605239);
                Ui.slider(class_3322, this.trackX0(), n5, this.trackX1() - this.trackX0(), n6, ((SliderRow)record).min(), ((SliderRow)record).max());
                continue;
            }
            class_3322.method_27535(this.field_22793, SwiftText.of(row.label()), 28, n5 + 9, -7697776);
        }
        if (this.module.hasPosition()) {
            n3 = this.field_22793.method_27525((class_5348)SwiftText.of("RESET POSITION")) + 24;
            boolean bl2 = this.inReset(n, n2);
            Ui.sprite(class_3322, bl2 ? Sprites.BTN_HOVER : Sprites.BTN, 24, this.field_22790 - 40, n3, 22);
            class_3322.method_27534(this.field_22793, SwiftText.of("RESET POSITION"), 24 + n3 / 2, this.field_22790 - 33, -855305);
        }
        String string = "Drag the sliders \u2014 your HUD updates live behind this menu";
        class_3322.method_27535(this.field_22793, SwiftText.of(string), this.field_22789 - this.field_22793.method_27525((class_5348)SwiftText.of(string)) - 24, this.field_22790 - 26, -7697776);
        super.method_25394(class_3322, n, n2, f);
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


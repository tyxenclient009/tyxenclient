/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2394
 *  net.minecraft.class_2396
 *  net.minecraft.class_2398
 *  net.minecraft.class_3940
 *  net.minecraft.class_702
 *  net.minecraft.class_703
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.tyxen.hud.mixin.client;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.modules.impl.render.Particles;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_3940;
import net.minecraft.class_702;
import net.minecraft.class_638;
import net.minecraft.class_638;
import net.minecraft.class_638;
import net.minecraft.class_703;
import net.minecraft.class_11939;
import net.minecraft.class_11939;
import net.minecraft.class_11939;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_702.class})
public abstract class ParticleEngineMixin {

    @Unique
    private static final Random tyxen_random = new Random();
    @Unique
    private static final ThreadLocal<Boolean> tyxen_isSpawningExtra = ThreadLocal.withInitial(() -> false);

    @Inject(method={"method_3056"}, at={@At(value="HEAD")}, cancellable=true)
    private <T extends class_2394> void onCreateParticle(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<class_703> cir) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        Particles module = mm.getModule(Particles.class);
        if (module == null || !module.isEnabled()) {
            return;
        }
        class_2396 type = particleData.method_10295();
        float multiplier = module.getMultiplier();
        if (multiplier < 1.0f && !tyxen_isSpawningExtra.get().booleanValue() && tyxen_random.nextFloat() > multiplier) {
            cir.setReturnValue(null);
            return;
        }
        if (this.shouldBlockParticle(module, type)) {
            cir.setReturnValue(null);
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_3056"}, at={@At(value="RETURN")})
    private <T extends class_2394> void afterCreateParticle(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<class_703> cir) {
        float multiplier;
        if (tyxen_isSpawningExtra.get().booleanValue()) {
            return;
        }
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        Particles module = mm.getModule(Particles.class);
        if (module == null || !module.isEnabled()) {
            return;
        }
        class_703 particle = (class_703)cir.getReturnValue();
        if (particle == null) {
            return;
        }
        class_2396 type = particleData.method_10295();
        if (module.useCustomCritColor() && (type == class_2398.field_11205 || type == class_2398.field_11209)) {
            int color = module.getCritColor();
            float r = (float)(color >> 16 & 0xFF) / 255.0f;
            float g = (float)(color >> 8 & 0xFF) / 255.0f;
            float b = (float)(color & 0xFF) / 255.0f;
            if (particle instanceof class_3940) {
                class_3940 singleQuadParticle = (class_3940)particle;
                singleQuadParticle.method_74305(r, g, b);
            }
        }
        if ((multiplier = module.getMultiplier()) > 1.0f) {
            int extraCount = (int)((multiplier - 1.0f) * 2.0f);
            tyxen_isSpawningExtra.set(true);
            try {
                class_702 engine = (class_702)(Object)this;
                for (int i = 0; i < extraCount; ++i) {
                    double offsetX = (tyxen_random.nextDouble() - 0.5) * 0.3;
                    double offsetY = (tyxen_random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (tyxen_random.nextDouble() - 0.5) * 0.3;
                    double velOffsetX = (tyxen_random.nextDouble() - 0.5) * 0.1;
                    double velOffsetY = (tyxen_random.nextDouble() - 0.5) * 0.1;
                    double velOffsetZ = (tyxen_random.nextDouble() - 0.5) * 0.1;
                    engine.method_3056(particleData, x + offsetX, y + offsetY, z + offsetZ, xSpeed + velOffsetX, ySpeed + velOffsetY, zSpeed + velOffsetZ);
                }
            }
            finally {
                tyxen_isSpawningExtra.set(false);
            }
        }
    }

    @Unique
    private boolean shouldBlockParticle(Particles module, class_2396<?> type) {
        if (type == class_2398.field_11205 || type == class_2398.field_11209) {
            return !module.shouldShowCriticals();
        }
        if (type == class_2398.field_11208) {
            return !module.shouldShowEnchanted();
        }
        if (type == class_2398.field_11236 || type == class_2398.field_11221) {
            return !module.shouldShowExplosion();
        }
        if (type == class_2398.field_11245 || type == class_2398.field_11213 || type == class_2398.field_11226) {
            return !module.shouldShowPotion();
        }
        if (type == class_2398.field_11248 || type == class_2398.field_17909) {
            return !module.shouldShowFirework();
        }
        if (type == class_2398.field_11220) {
            return !module.shouldShowTotem();
        }
        if (type == class_2398.field_11251 || type == class_2398.field_11237 || type == class_2398.field_17430 || type == class_2398.field_17431) {
            return !module.shouldShowSmoke();
        }
        if (type == class_2398.field_11240 || type == class_2398.field_22246 || type == class_2398.field_27783) {
            return !module.shouldShowFlame();
        }
        if (type == class_2398.field_11202 || type == class_2398.field_11244 || type == class_2398.field_11247 || type == class_2398.field_11241 || type == class_2398.field_11238 || type == class_2398.field_11243) {
            return !module.shouldShowWaterSplash();
        }
        if (type == class_2398.field_11242 || type == class_2398.field_11232 || type == class_2398.field_18306 || type == class_2398.field_28078 || type == class_2398.field_28079) {
            return !module.shouldShowWeather();
        }
        return false;
    }
}


package net.tyxen.hud.cosmetics;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.accessor.EntityRenderStateAccessor;
import net.tyxen.hud.modules.impl.render.WingsModule;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_583;

/**
 * Tyxen wings — Blockbench model rendered on equipped players' backs.
 * v1: static spread pose, fullbright, local + cached remote players.
 */
@Environment(value = EnvType.CLIENT)
public class WingsFeatureRenderer extends class_3887 {
    public static final String WINGS_ID = "tyxen-wings-neon";
    private static JsonModelRenderer model;

    public WingsFeatureRenderer(class_3883 ctx) {
        super(ctx);
    }

    private static synchronized JsonModelRenderer model() {
        if (model == null) {
            model = new JsonModelRenderer("/assets/tyxen/cosmetics/wings/model.json");
        }
        return model;
    }

    @Override
    public void method_4199(class_4587 matrices, class_11659 consumers, int light, class_10017 state, float limbAngle, float limbDistance) {
        try {
            if (!(state instanceof EntityRenderStateAccessor)) {
                return;
            }
            class_1297 entity = ((EntityRenderStateAccessor)state).tyxen$getEntity();
            if (!(entity instanceof class_1657)) {
                return;
            }
            String username = ((class_1657)entity).method_7334().name();
            if (username == null) {
                return;
            }
            if (!net.tyxen.hud.modules.impl.render.WingsModule.isActive()) {
                return;
            }
            if (!WingsFeatureRenderer.equipped(username, entity)) {
                return;
            }
            JsonModelRenderer m = WingsFeatureRenderer.model();
            if (m == null || !m.ready()) {
                return;
            }
            matrices.method_22903();
            matrices.method_46416(-0.5f, 0.85f, -0.8f);
            m.render(matrices, consumers);
            matrices.method_22909();
        } catch (Exception e) {
        }
    }

    private static boolean equipped(String username, class_1297 entity) {
        try {
            class_310 client = class_310.method_1551();
            if (client != null && entity == client.field_1724) {
                return net.tyxen.hud.store.StoreManager.getInstance().getState().getEquippedItemIds().contains(WINGS_ID);
            }
            java.util.List<String> eq = net.tyxen.hud.store.StoreManager.getInstance().equippedFor(username);
            return eq != null && eq.contains(WINGS_ID);
        } catch (Exception e) {
            return false;
        }
    }
}

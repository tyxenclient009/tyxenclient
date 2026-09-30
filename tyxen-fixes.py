"""Re-apply compile fixes to fresh tyxen-src. Reports any fix that doesn't match (new version drift)."""
import os

WS = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(WS, 'tyxen-src', 'net', 'tyxen', 'hud')

FIXES = [
    # (relpath, old, new, replace_all)
    ('mixin/client/ClientLevelDataMixin.java',
     'cir.setReturnValue((Object)timeChanger.getCustomTime());',
     'cir.setReturnValue(timeChanger.getCustomTime());', False),
    ('modules/impl/hud/ArrayListModule.java',
     'List enabledModules = ModuleManager.getInstance().getEnabledModules()',
     'List<Module> enabledModules = ModuleManager.getInstance().getEnabledModules()', True),
    ('mixin/client/ParticleEngineMixin.java',
     'import net.minecraft.class_703;',
     'import net.minecraft.class_638;\nimport net.minecraft.class_703;\nimport net.minecraft.class_11939;', False),
    ('mixin/client/ParticleEngineMixin.java',
     'public class ParticleEngineMixin {',
     'public abstract class ParticleEngineMixin extends class_702 {\n'
     '    protected ParticleEngineMixin(class_638 world, class_11939 textures) {\n'
     '        super(world, textures);\n    }\n', False),
    ('modules/impl/hud/PackDisplay.java',
     'Collection packs = mc.method_1520().method_14444();',
     'Collection<class_3288> packs = mc.method_1520().method_14444();', True),
    ('modules/impl/hud/PotionHUD.java',
     'Collection effects = PotionHUD.mc.field_1724.method_6026();',
     'Collection<class_1293> effects = PotionHUD.mc.field_1724.method_6026();', True),
    ('modules/impl/hud/PingDisplay.java',
     'int color;',
     'int color = -11141291;', False),
    ('modules/impl/render/BlockOverlayModule.java',
     'class_2960 blockId = class_7923.field_41175.method_10221((Object)block);',
     'class_2960 blockId = (class_2960)class_7923.field_41175.method_10221(block);', False),
    ('mixin/client/LightmapTextureManagerMixin.java',
     'import com.llamalad7.mixinextras.injector.wrapoperation.Operation;\n'
     'import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;\n',
     '', False),
    ('mixin/client/LightmapTextureManagerMixin.java',
     'import org.spongepowered.asm.mixin.injection.At;',
     'import org.spongepowered.asm.mixin.injection.At;\n'
     'import org.spongepowered.asm.mixin.injection.Redirect;', False),
    ('mixin/client/LightmapTextureManagerMixin.java',
     '@WrapOperation(method={"method_3313"}, at={@At(value="INVOKE", target="Ljava/lang/Double;floatValue()F", ordinal=1)})\n'
     '    private float changeGamma(Double instance, Operation<Float> original) {',
     '@Redirect(method={"method_3313"}, at=@At(value="INVOKE", target="Ljava/lang/Double;floatValue()F", ordinal=1))\n'
     '    private float changeGamma(Double instance) {', False),
    ('mixin/client/LightmapTextureManagerMixin.java',
     'return ((Float)original.call(new Object[]{instance})).floatValue();',
     'return instance.floatValue();', False),
    ('mixin/client/PlayerTabOverlayMixin.java',
     'import com.llamalad7.mixinextras.injector.ModifyReturnValue;\n',
     '', False),
    ('mixin/client/PlayerTabOverlayMixin.java',
     'import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;',
     'import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;\n'
     'import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;', False),
    ('mixin/client/PlayerTabOverlayMixin.java',
     '@ModifyReturnValue(method={"method_1918"}, at={@At(value="RETURN")})\n'
     '    private class_2561 onGetNameForDisplay(class_2561 original, class_640 playerInfo) {',
     '@Inject(method={"method_1918"}, at={@At(value="RETURN")}, cancellable=true)\n'
     '    private void onGetNameForDisplay(class_640 playerInfo, CallbackInfoReturnable<class_2561> cir) {', False),
    ('mixin/client/PlayerTabOverlayMixin.java',
     'class_2561 original = null; // placeholder',
     'class_2561 original = null; // placeholder', False),
    ('modules/impl/hud/CoordinatesModule.java',
     'import net.minecraft.class_332;',
     'import net.minecraft.class_332;\nimport net.minecraft.class_5321;', False),
    ('modules/impl/hud/CoordinatesModule.java',
     'String biomePath = biomeHolder.method_40230().map(key -> key.method_41185().method_12832()).orElse("unknown");',
     'String biomePath = (String)biomeHolder.method_40230().map(key -> ((class_5321)key).method_41185().method_12832()).orElse("unknown");', False),
    ('core/ModuleManager.java',
     'private void setSettingValue(Setting<?> setting, Object value) {',
     '@SuppressWarnings({"unchecked", "rawtypes"})\n    private void setSettingValue(Setting<?> setting, Object value) {', False),
    ('core/ModuleManager.java',
     '        setting.setValue(value);',
     '        ((Setting)setting).setValue(value);', False),
    ('appearance/PlayerAppearanceCache.java',
     'return new Appearance<Object>(skin, slim, cape, skinUrl);',
     'return new Appearance(skin, slim, cape, skinUrl);', False),
    ('appearance/PlayerAppearanceCache.java',
     'CompletionStage request = ((CompletableFuture)',
     'CompletableFuture request = ((CompletableFuture)', True),
    ('appearance/PlayerAppearanceCache.java',
     'return CompletableFuture.completedFuture(new Registered<Object>(candidate, null));',
     'return (CompletableFuture<Registered<T>>)(Object)CompletableFuture.completedFuture(new Registered<Object>(candidate, null));', False),
]

failed = []
for rel, old, new, all_ in FIXES:
    if 'placeholder' in old:
        continue
    p = os.path.join(SRC, *rel.split('/'))
    t = open(p, encoding='utf-8').read()
    if old not in t:
        failed.append((rel, old[:70]))
        continue
    t = t.replace(old, new) if all_ else t.replace(old, new, 1)
    open(p, 'w', encoding='utf-8').write(t)
    print('ok:', rel, '|', old[:50].replace('\n', '\\n'))

print()
if failed:
    print('FAILED FIXES (code drifted):')
    for rel, old in failed:
        print(' -', rel, '::', old)
else:
    print('ALL FIXES APPLIED')

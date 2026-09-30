package net.tyxen.hud.cosmetics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Matrix4fc;

/**
 * Minimal Blockbench-Java-model quad renderer for Tyxen cosmetics.
 * Static pose (rotations baked as-is when angle=0), no culling, fullbright.
 * Textures resolve under assets/tyxen/textures/.
 */
public final class JsonModelRenderer {
    private static final class Quad {
        final float[] p = new float[12];
        final float[] uv = new float[8];
        final float[] n = new float[3];
        String tex;
    }

    private final List<Quad> quads = new ArrayList<Quad>();
    private final Map<String, class_2960> textures = new HashMap<String, class_2960>();

    public JsonModelRenderer(String modelPath) {
        try {
            InputStream in = JsonModelRenderer.class.getResourceAsStream(modelPath);
            if (in == null) {
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            in.close();
            JsonArray size = root.has("texture_size") ? root.getAsJsonArray("texture_size") : null;
            float tw = size != null ? size.get(0).getAsFloat() : 16.0f;
            float th = size != null ? size.get(1).getAsFloat() : 16.0f;
            JsonObject texMap = root.has("textures") ? root.getAsJsonObject("textures") : new JsonObject();
            JsonArray elements = root.has("elements") ? root.getAsJsonArray("elements") : new JsonArray();
            for (JsonElement el : elements) {
                this.readElement(el.getAsJsonObject(), texMap, tw, th);
            }
        } catch (Exception e) {
        }
    }

    public boolean ready() {
        return !this.quads.isEmpty();
    }

    private String resolveTex(String ref, JsonObject texMap) {
        String key = ref.startsWith("#") ? ref.substring(1) : ref;
        String id = texMap.has(key) ? texMap.get(key).getAsString() : key;
        int ci = id.indexOf(58);
        String ns = ci >= 0 ? id.substring(0, ci) : "minecraft";
        String path = ci >= 0 ? id.substring(ci + 1) : id;
        if (!this.textures.containsKey(id)) {
            this.textures.put(id, class_2960.method_60655(ns, "textures/" + path + ".png"));
        }
        return id;
    }

    private void readElement(JsonObject el, JsonObject texMap, float tw, float th) {
        JsonArray from = el.getAsJsonArray("from");
        JsonArray to = el.getAsJsonArray("to");
        if (from == null || to == null) {
            return;
        }
        float x1 = from.get(0).getAsFloat() / 16.0f;
        float y1 = from.get(1).getAsFloat() / 16.0f;
        float z1 = from.get(2).getAsFloat() / 16.0f;
        float x2 = to.get(0).getAsFloat() / 16.0f;
        float y2 = to.get(1).getAsFloat() / 16.0f;
        float z2 = to.get(2).getAsFloat() / 16.0f;
        JsonObject faces = el.has("faces") ? el.getAsJsonObject("faces") : new JsonObject();
        this.face(faces, "north", texMap, tw, th,
                new float[]{x1, y2, z1, x2, y2, z1, x2, y1, z1, x1, y1, z1}, new float[]{0.0f, 0.0f, -1.0f});
        this.face(faces, "south", texMap, tw, th,
                new float[]{x2, y2, z2, x1, y2, z2, x1, y1, z2, x2, y1, z2}, new float[]{0.0f, 0.0f, 1.0f});
        this.face(faces, "west", texMap, tw, th,
                new float[]{x1, y2, z2, x1, y2, z1, x1, y1, z1, x1, y1, z2}, new float[]{-1.0f, 0.0f, 0.0f});
        this.face(faces, "east", texMap, tw, th,
                new float[]{x2, y2, z1, x2, y2, z2, x2, y1, z2, x2, y1, z1}, new float[]{1.0f, 0.0f, 0.0f});
        this.face(faces, "up", texMap, tw, th,
                new float[]{x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1}, new float[]{0.0f, 1.0f, 0.0f});
        this.face(faces, "down", texMap, tw, th,
                new float[]{x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2}, new float[]{0.0f, -1.0f, 0.0f});
    }

    private void face(JsonObject faces, String name, JsonObject texMap, float tw, float th, float[] pts, float[] n) {
        if (!faces.has(name)) {
            return;
        }
        JsonObject f = faces.getAsJsonObject(name);
        if (!f.has("uv") || !f.has("texture")) {
            return;
        }
        JsonArray uv = f.getAsJsonArray("uv");
        float u1 = uv.get(0).getAsFloat() / tw;
        float v1 = uv.get(1).getAsFloat() / th;
        float u2 = uv.get(2).getAsFloat() / tw;
        float v2 = uv.get(3).getAsFloat() / th;
        Quad q = new Quad();
        System.arraycopy(pts, 0, q.p, 0, 12);
        float[] uvs = new float[]{u1, v1, u2, v1, u2, v2, u1, v2};
        System.arraycopy(uvs, 0, q.uv, 0, 8);
        System.arraycopy(n, 0, q.n, 0, 3);
        q.tex = this.resolveTex(f.get("texture").getAsString(), texMap);
        this.quads.add(q);
    }

    private final Map<String, class_1921> layers = new HashMap<String, class_1921>();

    public void render(class_4587 matrices, class_11659 queue) {
        if (this.quads.isEmpty()) {
            return;
        }
        Map<String, List<Quad>> byTex = new HashMap<String, List<Quad>>();
        for (Quad q : this.quads) {
            List<Quad> list = byTex.get(q.tex);
            if (list == null) {
                list = new ArrayList<Quad>();
                byTex.put(q.tex, list);
            }
            list.add(q);
        }
        for (Map.Entry<String, List<Quad>> e : byTex.entrySet()) {
            class_1921 layer = this.layers.get(e.getKey());
            if (layer == null) {
                layer = class_12249.method_75994(this.textures.get(e.getKey()));
                this.layers.put(e.getKey(), layer);
            }
            final List<Quad> qs = e.getValue();
            queue.method_73483(matrices, layer, (pose, buf) -> {
                org.joml.Matrix4f matrix = pose.method_23761();
                for (Quad q : qs) {
                    for (int i = 0; i < 4; ++i) {
                        buf.method_22918((Matrix4fc)matrix, q.p[i * 3], q.p[i * 3 + 1], q.p[i * 3 + 2])
                                .method_39415(-1)
                                .method_22913(q.uv[i * 2], q.uv[i * 2 + 1])
                                .method_22921(0, 10)
                                .method_60796(15, 15)
                                .method_22914(q.n[0], q.n[1], q.n[2]);
                    }
                }
            });
        }
    }
}

/*
 * Decompiled with CFR 0.152.
 */
package net.tyxen.hud.render;

import java.util.function.Function;

public final class AnimationUtils {
    private AnimationUtils() {
    }

    public static float easeInOutCubic(float t) {
        return (t = AnimationUtils.clamp(t)) < 0.5f ? 4.0f * t * t * t : 1.0f - (float)Math.pow(-2.0 * (double)t + 2.0, 3.0) / 2.0f;
    }

    public static float easeOutCubic(float t) {
        return 1.0f - (float)Math.pow(1.0 - (double)AnimationUtils.clamp(t), 3.0);
    }

    public static float easeInCubic(float t) {
        t = AnimationUtils.clamp(t);
        return t * t * t;
    }

    public static float easeOutQuad(float t) {
        t = AnimationUtils.clamp(t);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    public static float easeInQuad(float t) {
        t = AnimationUtils.clamp(t);
        return t * t;
    }

    public static float easeOutExpo(float t) {
        t = AnimationUtils.clamp(t);
        return t >= 1.0f ? 1.0f : 1.0f - (float)Math.pow(2.0, -10.0 * (double)t);
    }

    public static float easeOutBack(float t) {
        t = AnimationUtils.clamp(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float)Math.pow(t - 1.0f, 3.0) + c1 * (float)Math.pow(t - 1.0f, 2.0);
    }

    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * AnimationUtils.clamp(progress);
    }

    public static float clamp(float t) {
        return Math.max(0.0f, Math.min(1.0f, t));
    }

    public static float smoothDelta(float current, float target, float speed, float delta) {
        float factor = 1.0f - (float)Math.pow(1.0 - (double)AnimationUtils.clamp(speed), delta);
        return current + (target - current) * factor;
    }

    /**
     * Apple-style spring (SwiftUI Spring mass = 1): interruptible,
     * frame-rate independent, settles with a soft overshoot like iOS.
     * Retarget mid-flight and it bends smoothly instead of snapping.
     */
    public static final class Spring {
        private float value;
        private float velocity;
        private float target;
        private float stiffness = 170.0f;
        private float damping = 22.0f;

        public Spring(float initial) {
            this.value = initial;
            this.velocity = 0.0f;
            this.target = initial;
        }

        public void animateTo(float target) {
            this.target = target;
        }

        public void reset(float value) {
            this.value = value;
            this.velocity = 0.0f;
            this.target = value;
        }

        public void setStiffness(float stiffness) {
            this.stiffness = stiffness;
        }

        public void setDamping(float damping) {
            this.damping = damping;
        }

        public float update(float dtSeconds) {
            float dt = Math.min(Math.max(dtSeconds, 0.0f), 0.05f);
            float force = -this.stiffness * (this.value - this.target) - this.damping * this.velocity;
            this.velocity += force * dt;
            this.value += this.velocity * dt;
            if (Math.abs(this.velocity) < 0.0005f && Math.abs(this.target - this.value) < 0.0005f) {
                this.value = this.target;
                this.velocity = 0.0f;
            }
            return this.value;
        }

        public float getValue() {
            return this.value;
        }

        public boolean atRest() {
            return this.value == this.target && this.velocity == 0.0f;
        }
    }

    public static class Animation {
        private float startValue;
        private float value;
        private float target;
        private long startTime;
        private long duration;
        private Function<Float, Float> easing;

        public Animation(float initialValue, long durationMs) {
            this.startValue = initialValue;
            this.value = initialValue;
            this.target = initialValue;
            this.duration = durationMs;
            this.startTime = System.currentTimeMillis();
            this.easing = AnimationUtils::easeOutCubic;
        }

        public void animateTo(float target) {
            if (this.target != target) {
                this.startValue = this.getValue();
                this.target = target;
                this.startTime = System.currentTimeMillis();
            }
        }

        public void reset(float value) {
            this.startValue = value;
            this.value = value;
            this.target = value;
            this.startTime = System.currentTimeMillis();
        }

        public float getValue() {
            long elapsed = System.currentTimeMillis() - this.startTime;
            if (elapsed >= this.duration) {
                this.value = this.target;
                return this.value;
            }
            float progress = (float)elapsed / (float)this.duration;
            progress = this.easing.apply(Float.valueOf(progress)).floatValue();
            return AnimationUtils.lerp(this.startValue, this.target, progress);
        }

        public float getProgress() {
            long elapsed = System.currentTimeMillis() - this.startTime;
            return AnimationUtils.clamp((float)elapsed / (float)this.duration);
        }

        public boolean isComplete() {
            return System.currentTimeMillis() - this.startTime >= this.duration;
        }

        public boolean isAnimating() {
            return !this.isComplete() && this.startValue != this.target;
        }

        public void setEasing(Function<Float, Float> easing) {
            this.easing = easing;
        }

        public void setDuration(long durationMs) {
            this.duration = durationMs;
        }

        public float getTarget() {
            return this.target;
        }

        public void toggle(float valueA, float valueB) {
            this.animateTo(this.target == valueA ? valueB : valueA);
        }
    }
}


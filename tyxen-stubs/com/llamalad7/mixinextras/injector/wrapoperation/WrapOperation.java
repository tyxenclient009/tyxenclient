package com.llamalad7.mixinextras.injector.wrapoperation;
import java.lang.annotation.*;
import org.spongepowered.asm.mixin.injection.At;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WrapOperation {
    String[] method() default {};
    At[] at() default {};
    boolean require() default true;
    int expect() default -1;
    int allow() default -1;
}

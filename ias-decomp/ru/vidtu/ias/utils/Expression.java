/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.CheckReturnValue
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package ru.vidtu.ias.utils;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Expression {
    @NotNull
    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    @NotNull
    private final String expression;
    private int pos = -1;
    private int ch;

    @Contract(pure=true)
    public Expression(@NotNull String expression) {
        this.expression = SPACE_PATTERN.matcher(expression).replaceAll("");
    }

    @CheckReturnValue
    public double parse() {
        try {
            this.next();
            double val = this.parseExpression();
            if (this.pos >= this.expression.length()) {
                return val;
            }
            throw new IllegalStateException("Read not fully: " + Character.toString(this.ch));
        }
        catch (Throwable t) {
            throw new IllegalStateException("Unable to parse: " + String.valueOf(this), t);
        }
    }

    @CheckReturnValue
    private double parseExpression() {
        double x = this.parseTerm();
        for (int i = 0; i < 64; ++i) {
            if (this.skipIf(43)) {
                x += this.parseTerm();
                continue;
            }
            if (this.skipIf(45)) {
                x -= this.parseTerm();
                continue;
            }
            return x;
        }
        throw new RuntimeException("Out of tries.");
    }

    @CheckReturnValue
    private double parseTerm() {
        double x = this.parseFactor();
        for (int i = 0; i < 64; ++i) {
            if (this.skipIf(42)) {
                x *= this.parseFactor();
                continue;
            }
            if (this.skipIf(47)) {
                x /= this.parseFactor();
                continue;
            }
            return x;
        }
        throw new RuntimeException("Out of tries.");
    }

    @CheckReturnValue
    private double parseFactor() {
        double x;
        if (this.skipIf(43)) {
            return this.parseFactor();
        }
        if (this.skipIf(45)) {
            return -this.parseFactor();
        }
        int startPos = this.pos;
        if (this.skipIf(40)) {
            x = this.parseExpression();
            if (!this.skipIf(41)) {
                throw new RuntimeException("Missing ')'");
            }
        } else if (this.ch >= 48 && this.ch <= 57 || this.ch == 46) {
            for (int i = 0; i < 64 && (this.ch >= 48 && this.ch <= 57 || this.ch == 46); ++i) {
                this.next();
            }
            x = Double.parseDouble(this.expression.substring(startPos, this.pos));
        } else if (this.ch >= 97 && this.ch <= 122) {
            for (int i = 0; i < 64 && this.ch >= 97 && this.ch <= 122; ++i) {
                this.next();
            }
            String func = this.expression.substring(startPos, this.pos);
            if (this.skipIf(40)) {
                x = this.parseExpression();
                if (!this.skipIf(41)) {
                    throw new RuntimeException("Missing ')' after argument to " + func);
                }
            } else {
                x = this.parseFactor();
            }
            x = switch (func) {
                case "sqrt" -> Math.sqrt(x);
                case "sin" -> Math.sin(Math.toRadians(x));
                case "cos" -> Math.cos(Math.toRadians(x));
                case "tan" -> Math.tan(Math.toRadians(x));
                default -> throw new RuntimeException("Unknown function: " + func);
            };
        } else {
            throw new RuntimeException("Unexpected: " + Character.toString(this.ch));
        }
        if (this.skipIf(94)) {
            x = Math.pow(x, this.parseFactor());
        }
        return x;
    }

    @CheckReturnValue
    private boolean skipIf(int ch) {
        for (int i = 0; i < 1024 && Character.isWhitespace(ch); ++i) {
            this.next();
        }
        if (this.ch != ch) {
            return false;
        }
        this.next();
        return true;
    }

    private void next() {
        ++this.pos;
        this.ch = this.pos < this.expression.length() ? this.expression.codePointAt(this.pos) : -1;
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "Expression{expression='" + this.expression + "', pos=" + this.pos + ", ch=" + this.ch + "}";
    }

    @Contract(pure=true)
    public static double parse(@NotNull String expression) {
        Expression expr = new Expression(expression);
        return expr.parse();
    }

    @Contract(pure=true)
    @Nullable
    public static Integer parsePosition(@Nullable String expression, int width, int height) {
        try {
            if (expression == null || expression.isBlank()) {
                return null;
            }
            return (int)Expression.parse(expression.toLowerCase(Locale.ROOT).replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height)));
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    @Contract(pure=true)
    public static int positionValidityColor(@Nullable String expression, int width, int height, boolean x) {
        try {
            int bound;
            if (expression == null || expression.isBlank()) {
                return -8355712;
            }
            double value = Expression.parse(expression.toLowerCase(Locale.ROOT).replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height)));
            if (!Double.isFinite(value) || value < -2.147483648E9 || value > 2.147483647E9) {
                return Short.MIN_VALUE;
            }
            int rounded = (int)value;
            if (rounded < 0) {
                return -256;
            }
            int n = bound = x ? width : height;
            if (value > (double)bound) {
                return -256;
            }
            return -2039584;
        }
        catch (Throwable ignored) {
            return -65536;
        }
    }
}


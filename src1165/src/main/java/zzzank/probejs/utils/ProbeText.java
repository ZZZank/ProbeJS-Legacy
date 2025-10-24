package zzzank.probejs.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author ZZZank
 */
@SuppressWarnings("unused")
public final class ProbeText {
    private static final Object[] EMPTY_ARGS = new Object[0];

    public static ProbeText of(@NotNull MutableComponent raw) {
        return new ProbeText(Objects.requireNonNull(raw));
    }

    public static ProbeText literal(String literal) {
        return of(new TextComponent(literal));
    }

    public static ProbeText translatable(String key, Object... args) {
        return of(new TranslatableComponent(key, args));
    }

    public static ProbeText pjs(String key, Object... args) {
        return translatable("probejs." + key, args);
    }

    public static ProbeText pjs(String key) {
        return pjs(key, EMPTY_ARGS);
    }

    private MutableComponent raw;

    private ProbeText(MutableComponent raw) {
        this.raw = raw;
    }

    public ProbeText setStyle(Style style) {
        raw = raw.setStyle(style);
        return this;
    }

    public ProbeText append(Component component) {
        this.raw = raw.append(component);
        return this;
    }

    public ProbeText append(String literal) {
        return append(new TextComponent(literal));
    }

    public ProbeText append(ProbeText component) {
        return append(component.raw);
    }

    public ProbeText color(TextColor color) {
        return setStyle(getStyle().withColor(color));
    }

    public ProbeText color(ChatFormatting color) {
        return setStyle(getStyle().withColor(color));
    }

    public ProbeText click(ClickEvent clickEvent) {
        return setStyle(getStyle().withClickEvent(clickEvent));
    }

    public ProbeText click(ClickEvent.Action action, String str) {
        return click(new ClickEvent(action, str));
    }

    public ProbeText hover(HoverEvent hoverEvent) {
        return setStyle(getStyle().withHoverEvent(hoverEvent));
    }

    public <T> ProbeText hover(HoverEvent.Action<T> action, T object) {
        return hover(new HoverEvent(action, object));
    }

    public ProbeText font(ResourceLocation fontId) {
        return setStyle(getStyle().withFont(fontId));
    }

    public ProbeText insertion(String insertion) {
        return setStyle(getStyle().withInsertion(insertion));
    }

    public ProbeText bold(Boolean bold) {
        return setStyle(getStyle().withBold(bold));
    }

    public ProbeText italic(Boolean italic) {
        return setStyle(getStyle().withItalic(italic));
    }

    public ProbeText underlined(Boolean underlined) {
        return setStyle(getStyle().setUnderlined(underlined));
    }

    public ProbeText strikethrough(Boolean strikethrough) {
        return setStyle(getStyle().setStrikethrough(strikethrough));
    }

    public ProbeText obfuscated(Boolean obfuscated) {
        return setStyle(getStyle().setObfuscated(obfuscated));
    }

    public ProbeText black() {
        return this.color(ChatFormatting.BLACK);
    }

    public ProbeText darkBlue() {
        return this.color(ChatFormatting.DARK_BLUE);
    }

    public ProbeText darkGreen() {
        return this.color(ChatFormatting.DARK_GREEN);
    }

    public ProbeText darkAqua() {
        return this.color(ChatFormatting.DARK_AQUA);
    }

    public ProbeText darkRed() {
        return this.color(ChatFormatting.DARK_RED);
    }

    public ProbeText darkPurple() {
        return this.color(ChatFormatting.DARK_PURPLE);
    }

    public ProbeText gold() {
        return this.color(ChatFormatting.GOLD);
    }

    public ProbeText gray() {
        return this.color(ChatFormatting.GRAY);
    }

    public ProbeText darkGray() {
        return this.color(ChatFormatting.DARK_GRAY);
    }

    public ProbeText blue() {
        return this.color(ChatFormatting.BLUE);
    }

    public ProbeText green() {
        return this.color(ChatFormatting.GREEN);
    }

    public ProbeText aqua() {
        return this.color(ChatFormatting.AQUA);
    }

    public ProbeText red() {
        return this.color(ChatFormatting.RED);
    }

    public ProbeText lightPurple() {
        return this.color(ChatFormatting.LIGHT_PURPLE);
    }

    public ProbeText yellow() {
        return this.color(ChatFormatting.YELLOW);
    }

    public ProbeText white() {
        return this.color(ChatFormatting.WHITE);
    }

    public MutableComponent unwrap() {
        return raw;
    }

    public Style getStyle() {
        return raw.getStyle();
    }
}

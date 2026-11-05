package zzzank.probejs.features.minecraft;

/// A window of at most `maxRows` lines over a list of values, used by
/// [ConfigButton] to render the "list of enum values" tooltip.
///
/// The selected value is kept as close to the middle as possible, and at most one
/// line is reserved for the `... (N more values)` ellipsis, so the rendered tooltip
/// never exceeds `maxRows` lines.
///
/// `hidden` is always the total amount of values that are not rendered, regardless
/// of whether they are hidden above or below the window.
///
/// @param start   index of the first rendered value
/// @param shown   amount of rendered values
/// @param ellipsisAtTop whether the ellipsis line is rendered above the values
/// @param hidden  amount of values that are not rendered
/// @author ZZZank
public record EnumValueWindow(int start, int shown, boolean ellipsisAtTop, int hidden) {

    /// @param total    amount of values
    /// @param selected index of the selected value, out of range values are clamped
    /// @param maxRows  maximum amount of rendered lines, including the ellipsis line
    public static EnumValueWindow of(int total, int selected, int maxRows) {
        if (total <= 0) {
            return new EnumValueWindow(0, 0, false, 0);
        }
        int budget = Math.max(2, maxRows);
        if (total <= budget) {
            return new EnumValueWindow(0, total, false, 0);
        }

        int clamped = Math.max(0, Math.min(selected, total - 1));
        int shown = budget - 1; // reserve one line for the ellipsis
        int start = Math.max(0, Math.min(clamped - budget / 2, total - shown));
        // when the window is stuck to the tail, the hidden values are all above it
        boolean ellipsisAtTop = start + shown >= total;
        return new EnumValueWindow(start, shown, ellipsisAtTop, total - shown);
    }

    /// amount of rendered lines, including the ellipsis line
    public int lineCount() {
        return this.shown + (this.hidden > 0 ? 1 : 0);
    }
}

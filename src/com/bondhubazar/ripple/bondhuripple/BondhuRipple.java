
package com.bondhubazar.ripple.bondhuripple;

// Android Graphics
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;

// Android View
import android.view.View;

// Java Collection
import java.util.Map;
import java.util.WeakHashMap;

// App Inventor Imports
import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

@Extension
@DesignerComponent(
    version = 1,
    versionName = "1.0",
    description = "Universal Ripple Effect Extension for Kodular and App Inventor",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)

public class BondhuRipple extends AndroidNonvisibleComponent {

    // Android Context
    private final android.content.Context context;

    // Original View State
    private static class RippleState {

        Drawable originalBackground;
        boolean originalClickable;

        int paddingLeft;
        int paddingTop;
        int paddingRight;
        int paddingBottom;

        boolean rippleApplied;
    }

    // Registered Views
    private final Map<View, RippleState> rippleViews =
            new WeakHashMap<View, RippleState>();

    // Constructor
    public BondhuRipple(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
    }

    // 1. Register View
    @SimpleFunction(description = "Register a Button or Arrangement for Ripple Effect")
    public void RegisterView(AndroidViewComponent component) {

        try {
            if (component == null) {
                ErrorOccurred("RegisterView: Component is null");
                return;
            }

            View view = component.getView();

            if (view == null) {
                ErrorOccurred("RegisterView: View is null");
                return;
            }

            if (!rippleViews.containsKey(view)) {

                RippleState state = new RippleState();

                state.originalBackground = view.getBackground();
                state.originalClickable = view.isClickable();

                state.paddingLeft = view.getPaddingLeft();
                state.paddingTop = view.getPaddingTop();
                state.paddingRight = view.getPaddingRight();
                state.paddingBottom = view.getPaddingBottom();

                state.rippleApplied = false;

                rippleViews.put(view, state);
            }

            ViewRegistered("View successfully registered");

        } catch (Exception e) {
            ErrorOccurred("RegisterView: " + e.getMessage());
        }
    }

    // 2. Set Ripple
    @SimpleFunction(description = "Apply Ripple Effect to registered View")
    public void SetRipple(
        AndroidViewComponent component,
        String rippleColor,
        int cornerRadiusDp
    ) {

        try {
            if (component == null) {
                ErrorOccurred("SetRipple: Component is null");
                return;
            }

            View view = component.getView();

            if (view == null) {
                ErrorOccurred("SetRipple: View is null");
                return;
            }

            // View Register না থাকলে Register করা
            if (!rippleViews.containsKey(view)) {
                RegisterView(component);
            }

            RippleState state = rippleViews.get(view);

            if (state == null) {
                ErrorOccurred("SetRipple: State not found");
                return;
            }

            // Ripple Color
            int color = Color.parseColor(rippleColor);
            ColorStateList colorState = ColorStateList.valueOf(color);

            // Corner Radius
            float radius = dp(Math.max(0, cornerRadiusDp));

            // Ripple Mask
            GradientDrawable mask = new GradientDrawable();
            mask.setShape(GradientDrawable.RECTANGLE);
            mask.setColor(Color.WHITE);
            mask.setCornerRadius(radius);

            // Original Background
            Drawable original = state.originalBackground;

            if (original == null) {

                GradientDrawable transparent = new GradientDrawable();
                transparent.setColor(Color.TRANSPARENT);
                transparent.setCornerRadius(radius);

                original = transparent;
            }

            // Ripple Drawable
            RippleDrawable rippleDrawable = new RippleDrawable(
                colorState,
                original,
                mask
            );

            // Apply Ripple
            view.setBackground(rippleDrawable);

            // Original Padding Restore
            view.setPadding(
                state.paddingLeft,
                state.paddingTop,
                state.paddingRight,
                state.paddingBottom
            );

            // Touch Enable
            view.setClickable(true);

            state.rippleApplied = true;

            RippleApplied("Ripple successfully applied");

        } catch (Exception e) {
            ErrorOccurred("SetRipple: " + e.getMessage());
        }
    }

    // 3. Remove Ripple
    @SimpleFunction(description = "Remove Ripple and restore original background")
    public void RemoveRipple(AndroidViewComponent component) {

        try {
            if (component == null) {
                ErrorOccurred("RemoveRipple: Component is null");
                return;
            }

            View view = component.getView();

            if (view == null) {
                ErrorOccurred("RemoveRipple: View is null");
                return;
            }

            RippleState state = rippleViews.get(view);

            if (state == null) {
                ErrorOccurred("RemoveRipple: View not registered");
                return;
            }

            // Original Background Restore
            view.setBackground(state.originalBackground);

            // Original Padding Restore
            view.setPadding(
                state.paddingLeft,
                state.paddingTop,
                state.paddingRight,
                state.paddingBottom
            );

            // Original Clickable Restore
            view.setClickable(state.originalClickable);

            state.rippleApplied = false;

            // Registration Remove
            rippleViews.remove(view);

            RippleRemoved("Ripple successfully removed");

        } catch (Exception e) {
            ErrorOccurred("RemoveRipple: " + e.getMessage());
        }
    }

    // 4. Check Ripple Applied
    @SimpleFunction(description = "Check whether Ripple is applied to a View")
    public boolean IsRippleApplied(AndroidViewComponent component) {

        if (component == null) {
            return false;
        }

        View view = component.getView();

        if (view == null) {
            return false;
        }

        RippleState state = rippleViews.get(view);

        return state != null
                && state.rippleApplied
                && view.getBackground() instanceof RippleDrawable;
    }

    // 5. Clear All Ripples
    @SimpleFunction(description = "Remove Ripple from all registered Views")
    public void ClearAllRipples() {

        try {

            for (Map.Entry<View, RippleState> entry :
                    rippleViews.entrySet()) {

                View view = entry.getKey();
                RippleState state = entry.getValue();

                if (view != null && state != null) {

                    // Original Background Restore
                    view.setBackground(state.originalBackground);

                    // Original Padding Restore
                    view.setPadding(
                        state.paddingLeft,
                        state.paddingTop,
                        state.paddingRight,
                        state.paddingBottom
                    );

                    // Original Clickable Restore
                    view.setClickable(state.originalClickable);
                }
            }

            rippleViews.clear();

            AllRipplesCleared();

        } catch (Exception e) {
            ErrorOccurred("ClearAllRipples: " + e.getMessage());
        }
    }

    // 6. View Registered Event
    @SimpleEvent(description = "When a View is registered")
    public void ViewRegistered(String message) {
        EventDispatcher.dispatchEvent(this, "ViewRegistered", message);
    }

    // 7. Ripple Applied Event
    @SimpleEvent(description = "When Ripple is applied")
    public void RippleApplied(String message) {
        EventDispatcher.dispatchEvent(this, "RippleApplied", message);
    }

    // 8. Ripple Removed Event
    @SimpleEvent(description = "When Ripple is removed")
    public void RippleRemoved(String message) {
        EventDispatcher.dispatchEvent(this, "RippleRemoved", message);
    }

    // 9. All Ripples Cleared Event
    @SimpleEvent(description = "When all Ripples are removed")
    public void AllRipplesCleared() {
        EventDispatcher.dispatchEvent(this, "AllRipplesCleared");
    }

    // 10. Error Event
    @SimpleEvent(description = "When an error occurs")
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", message);
    }

    // 11. DP to Pixel
    private int dp(float value) {
        return (int) (
            value * context.getResources()
                .getDisplayMetrics().density + 0.5f
        );
    }
}

package me.protonplus.lumin.util.stickycards;

import javafx.scene.paint.Color;

public class StickyCardColor {
    private Color primaryColor;
    private Color secondaryColor;

    public static final Color COMPLETE_COLOR = Color.web("#00CA4E");

    public static final StickyCardColor YELLOW_THEME = new StickyCardColor(Color.web("#F4DC67"), Color.web("#FFE66D"));
    public static final StickyCardColor BLUE_THEME = new StickyCardColor(Color.web("#8BC2DC"), Color.web("#9EDFFE"));
    public static final StickyCardColor PINK_THEME = new StickyCardColor(Color.web("#D692BB"), Color.web("#FDB0DF"));

    public StickyCardColor(Color primaryColor, Color secondaryColor) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }
}

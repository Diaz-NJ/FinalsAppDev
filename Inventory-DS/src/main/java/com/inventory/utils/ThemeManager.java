package main.java.com.inventory.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public enum ThemeMode {
        LIGHT,
        DARK
    }

    private static ThemeMode currentTheme = ThemeMode.LIGHT;
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();

    // Color schemes
    private static final Color LIGHT_BACKGROUND = Color.WHITE;
    private static final Color LIGHT_FOREGROUND = Color.BLACK;
    private static final Color LIGHT_BUTTON_BG = new Color(200, 200, 200);
    private static final Color LIGHT_BUTTON_FG = Color.BLACK;
    private static final Color LIGHT_TABLE_BG = Color.WHITE;
    private static final Color LIGHT_TABLE_FG = Color.BLACK;

    private static final Color DARK_BACKGROUND = new Color(30, 30, 30);
    private static final Color DARK_FOREGROUND = Color.WHITE;
    private static final Color DARK_BUTTON_BG = new Color(50, 50, 50);
    private static final Color DARK_BUTTON_FG = Color.WHITE;
    private static final Color DARK_TABLE_BG = new Color(40, 40, 40);
    private static final Color DARK_TABLE_FG = Color.WHITE;

    public interface ThemeChangeListener {
        void onThemeChanged(ThemeMode newTheme);
    }

    public static void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    public static void setTheme(ThemeMode theme) {
        currentTheme = theme;
        applyTheme();
        notifyListeners();
    }

    public static ThemeMode getCurrentTheme() {
        return currentTheme;
    }

    private static void applyTheme() {
        Color background = currentTheme == ThemeMode.LIGHT ? LIGHT_BACKGROUND : DARK_BACKGROUND;
        Color foreground = currentTheme == ThemeMode.LIGHT ? LIGHT_FOREGROUND : DARK_FOREGROUND;
        Color buttonBg = currentTheme == ThemeMode.LIGHT ? LIGHT_BUTTON_BG : DARK_BUTTON_BG;
        Color buttonFg = currentTheme == ThemeMode.LIGHT ? LIGHT_BUTTON_FG : DARK_BUTTON_FG;
        Color tableBg = currentTheme == ThemeMode.LIGHT ? LIGHT_TABLE_BG : DARK_TABLE_BG;
        Color tableFg = currentTheme == ThemeMode.LIGHT ? LIGHT_TABLE_FG : DARK_TABLE_FG;

        UIManager.put("Panel.background", background);
        UIManager.put("Panel.foreground", foreground);
        UIManager.put("Button.background", buttonBg);
        UIManager.put("Button.foreground", buttonFg);
        UIManager.put("TextField.background", background);
        UIManager.put("TextField.foreground", foreground);
        UIManager.put("ComboBox.background", background);
        UIManager.put("ComboBox.foreground", foreground);
        UIManager.put("Table.background", tableBg);
        UIManager.put("Table.foreground", tableFg);
        UIManager.put("Table.gridColor", foreground);
        UIManager.put("TableHeader.background", buttonBg);
        UIManager.put("TableHeader.foreground", buttonFg);
    }

    private static void notifyListeners() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }
}
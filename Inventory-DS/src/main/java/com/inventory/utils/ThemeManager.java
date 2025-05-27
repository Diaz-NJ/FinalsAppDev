package main.java.com.inventory.utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public enum ThemeMode { LIGHT, DARK }
    private static ThemeMode currentTheme = ThemeMode.LIGHT;
    public static final Map<String, Color> LIGHT_COLORS = new HashMap<>();
    public static final Map<String, Color> DARK_COLORS = new HashMap<>();
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();

    static {
        // Define light theme colors
        LIGHT_COLORS.put("Panel.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("Button.background", new Color(200, 200, 200));
        LIGHT_COLORS.put("Button.foreground", Color.BLACK);
        LIGHT_COLORS.put("Label.foreground", Color.BLACK);
        LIGHT_COLORS.put("TextField.background", Color.WHITE);
        LIGHT_COLORS.put("TextField.foreground", Color.BLACK);
        LIGHT_COLORS.put("Table.background", new Color(255, 255, 255));
        LIGHT_COLORS.put("Table.foreground", Color.BLACK);
        LIGHT_COLORS.put("Table.gridColor", new Color(200, 200, 200));
        LIGHT_COLORS.put("TabbedPane.background", new Color(220, 220, 220));
        LIGHT_COLORS.put("TabbedPane.foreground", Color.BLACK);
        LIGHT_COLORS.put("ToolBar.background", new Color(230, 230, 230));
        LIGHT_COLORS.put("ScrollPane.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("Dialog.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("OptionPane.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("OptionPane.foreground", Color.BLACK);
        LIGHT_COLORS.put("ComboBox.background", Color.WHITE);
        LIGHT_COLORS.put("ComboBox.foreground", Color.BLACK);

        // Define dark theme colors
        DARK_COLORS.put("Panel.background", new Color(40, 40, 40));
        DARK_COLORS.put("Button.background", new Color(70, 70, 70));
        DARK_COLORS.put("Button.foreground", Color.WHITE);
        DARK_COLORS.put("Label.foreground", Color.WHITE);
        DARK_COLORS.put("TextField.background", new Color(60, 60, 60));
        DARK_COLORS.put("TextField.foreground", Color.WHITE);
        DARK_COLORS.put("Table.background", new Color(50, 50, 50));
        DARK_COLORS.put("Table.foreground", Color.WHITE);
        DARK_COLORS.put("Table.gridColor", new Color(90, 90, 90));
        DARK_COLORS.put("TabbedPane.background", new Color(30, 30, 30));
        DARK_COLORS.put("TabbedPane.foreground", Color.WHITE);
        DARK_COLORS.put("ToolBar.background", new Color(50, 50, 50));
        DARK_COLORS.put("ScrollPane.background", new Color(40, 40, 40));
        DARK_COLORS.put("Dialog.background", new Color(40, 40, 40));
        DARK_COLORS.put("OptionPane.background", new Color(40, 40, 40));
        DARK_COLORS.put("OptionPane.foreground", Color.WHITE);
        DARK_COLORS.put("ComboBox.background", new Color(60, 60, 60));
        DARK_COLORS.put("ComboBox.foreground", Color.WHITE);
    }

    public static void setTheme(ThemeMode theme) {
        System.out.println("[DEBUG] Setting theme to: " + theme);
        currentTheme = theme;
        Map<String, Color> colors = (theme == ThemeMode.LIGHT) ? LIGHT_COLORS : DARK_COLORS;

        // Apply colors to UIManager
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            UIManager.put(entry.getKey(), entry.getValue());
            System.out.println("[DEBUG] Applied " + entry.getKey() + " = " + entry.getValue());
        }

        // Force update of all UI components, including dialogs
        for (Frame frame : Frame.getFrames()) {
            SwingUtilities.updateComponentTreeUI(frame);
            frame.repaint();
            frame.revalidate();
        }

        // Notify all listeners
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(theme);
        }
    }

    public static ThemeMode getCurrentTheme() {
        return currentTheme;
    }

    public static void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void applyThemeToComponent(Component component) {
        Map<String, Color> colors = (currentTheme == ThemeMode.LIGHT) ? LIGHT_COLORS : DARK_COLORS;
        if (component instanceof JFrame) {
            JFrame frame = (JFrame) component;
            frame.getContentPane().setBackground(colors.get("Panel.background"));
            for (Component child : frame.getContentPane().getComponents()) {
                applyThemeToComponent(child);
            }
        } else if (component instanceof JToolBar) {
            JToolBar toolBar = (JToolBar) component;
            toolBar.setBackground(colors.get("ToolBar.background"));
            for (Component child : toolBar.getComponents()) {
                applyThemeToComponent(child);
            }
        } else if (component instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) component;
            tabbedPane.setBackground(colors.get("TabbedPane.background"));
            tabbedPane.setForeground(colors.get("TabbedPane.foreground"));
            for (Component tab : tabbedPane.getComponents()) {
                applyThemeToComponent(tab);
            }
        } else if (component instanceof JPanel) {
            component.setBackground(colors.get("Panel.background"));
        } else if (component instanceof JButton) {
            component.setBackground(colors.get("Button.background"));
            component.setForeground(colors.get("Button.foreground"));
        } else if (component instanceof JTextField) {
            component.setBackground(colors.get("TextField.background"));
            component.setForeground(colors.get("TextField.foreground"));
        } else if (component instanceof JLabel) {
            component.setForeground(colors.get("Label.foreground"));
        } else if (component instanceof JComboBox) {
            component.setBackground(colors.get("ComboBox.background"));
            component.setForeground(colors.get("ComboBox.foreground"));
        }
        SwingUtilities.updateComponentTreeUI(component);
        component.repaint();
        component.revalidate();
    }

    public interface ThemeChangeListener {
        void onThemeChanged(ThemeMode newTheme);
    }
}
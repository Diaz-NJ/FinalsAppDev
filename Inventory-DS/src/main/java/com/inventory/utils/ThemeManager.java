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
        LIGHT_COLORS.put("TextField.caret", Color.BLACK); // Added for caret color
        LIGHT_COLORS.put("Table.background", new Color(255, 255, 255));
        LIGHT_COLORS.put("Table.foreground", new Color(0, 0, 0));
        LIGHT_COLORS.put("Table.gridColor", new Color(180, 180, 180));
        LIGHT_COLORS.put("TabbedPane.background", new Color(220, 220, 220));
        LIGHT_COLORS.put("TabbedPane.foreground", new Color(0, 0, 0));
        LIGHT_COLORS.put("ToolBar.background", new Color(230, 230, 230));
        LIGHT_COLORS.put("ScrollPane.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("Dialog.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("OptionPane.background", new Color(240, 240, 240));
        LIGHT_COLORS.put("OptionPane.foreground", Color.BLACK);
        LIGHT_COLORS.put("ComboBox.background", Color.WHITE);
        LIGHT_COLORS.put("ComboBox.foreground", Color.BLACK);
        LIGHT_COLORS.put("ComboBox.selectionBackground", new Color(184, 207, 229));
        LIGHT_COLORS.put("ComboBox.selectionForeground", Color.BLACK);

        // Define dark theme colors
        DARK_COLORS.put("Panel.background", new Color(40, 40, 40));
        DARK_COLORS.put("Button.background", new Color(70, 70, 70));
        DARK_COLORS.put("Button.foreground", Color.WHITE);
        DARK_COLORS.put("Label.foreground", Color.WHITE);
        DARK_COLORS.put("TextField.background", new Color(80, 80, 80)); // Lighter gray for better contrast
        DARK_COLORS.put("TextField.foreground", Color.WHITE);
        DARK_COLORS.put("TextField.caret", Color.WHITE); // Added for caret visibility
        DARK_COLORS.put("Table.background", new Color(50, 50, 50));
        DARK_COLORS.put("Table.foreground", new Color(230, 230, 230));
        DARK_COLORS.put("Table.gridColor", new Color(100, 100, 100));
        DARK_COLORS.put("TabbedPane.background", new Color(30, 30, 30));
        DARK_COLORS.put("TabbedPane.foreground", new Color(200, 200, 200));
        DARK_COLORS.put("ToolBar.background", new Color(50, 50, 50));
        DARK_COLORS.put("ScrollPane.background", new Color(40, 40, 40));
        DARK_COLORS.put("Dialog.background", new Color(40, 40, 40));
        DARK_COLORS.put("OptionPane.background", new Color(40, 40, 40));
        DARK_COLORS.put("OptionPane.foreground", Color.WHITE);
        DARK_COLORS.put("ComboBox.background", new Color(60, 60, 60));
        DARK_COLORS.put("ComboBox.foreground", Color.WHITE);
        DARK_COLORS.put("ComboBox.selectionBackground", new Color(80, 80, 80));
        DARK_COLORS.put("ComboBox.selectionForeground", Color.WHITE);
    }

    public static void setTheme(ThemeMode theme) {
        if (currentTheme == theme) {
            System.out.println("[DEBUG] Theme already set to: " + theme + ", skipping update");
            return;
        }

        System.out.println("[DEBUG] Setting theme to: " + theme);
        currentTheme = theme;
        Map<String, Color> colors = (theme == ThemeMode.LIGHT) ? LIGHT_COLORS : DARK_COLORS;

        // Apply generic UIManager properties
        UIManager.put("Panel.background", colors.get("Panel.background"));
        UIManager.put("Button.background", colors.get("Button.background"));
        UIManager.put("Button.foreground", colors.get("Button.foreground"));
        UIManager.put("TextField.background", colors.get("TextField.background"));
        UIManager.put("TextField.foreground", colors.get("TextField.foreground"));
        UIManager.put("TextField.caretForeground", colors.get("TextField.caret")); // Added for caret
        UIManager.put("ComboBox.background", colors.get("ComboBox.background"));
        UIManager.put("ComboBox.foreground", colors.get("ComboBox.foreground"));
        UIManager.put("ComboBox.selectionBackground", colors.get("ComboBox.selectionBackground"));
        UIManager.put("ComboBox.selectionForeground", colors.get("ComboBox.selectionForeground"));
        UIManager.put("OptionPane.background", colors.get("OptionPane.background"));
        UIManager.put("OptionPane.foreground", colors.get("OptionPane.foreground"));

        // Apply Nimbus-specific properties only if Nimbus is active
        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        if (currentLookAndFeel != null && currentLookAndFeel.getClass().getName().equals("javax.swing.plaf.nimbus.NimbusLookAndFeel")) {
            UIManager.put("control", colors.get("Panel.background"));
            UIManager.put("nimbusBase", colors.get("Button.background"));
            UIManager.put("nimbusBlueGrey", colors.get("Button.background"));
            UIManager.put("text", colors.get("Button.foreground"));
            UIManager.put("ComboBox:\"ComboBox.renderer\"[Selected].background", colors.get("ComboBox.selectionBackground"));
            UIManager.put("ComboBox:\"ComboBox.renderer\"[Selected].textForeground", colors.get("ComboBox.selectionForeground"));
            UIManager.put("TextField.caretForeground", colors.get("TextField.caret")); // Added for Nimbus
            System.out.println("[DEBUG] Applied Nimbus-specific UIManager properties");
        } else {
            System.out.println("[DEBUG] NimbusLookAndFeel not active, skipping Nimbus-specific properties");
        }

        // Force update of all UI components
        for (Frame frame : Frame.getFrames()) {
            SwingUtilities.updateComponentTreeUI(frame);
            for (Window window : frame.getOwnedWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            frame.repaint();
            frame.revalidate();
            System.out.println("[DEBUG] Updated frame: " + frame.getClass().getSimpleName());
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
            System.out.println("[DEBUG] Added theme change listener: " + listener.getClass().getSimpleName());
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
        } else if (component instanceof JDialog) {
            JDialog dialog = (JDialog) component;
            dialog.getContentPane().setBackground(colors.get("Dialog.background"));
            for (Component child : dialog.getContentPane().getComponents()) {
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
            for (Component child : ((JPanel) component).getComponents()) {
                applyThemeToComponent(child);
            }
        } else if (component instanceof JButton) {
            component.setBackground(colors.get("Button.background"));
            component.setForeground(colors.get("Button.foreground"));
        } else if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setBackground(colors.get("TextField.background"));
            textField.setForeground(colors.get("TextField.foreground"));
            textField.setCaretColor(colors.get("TextField.caret")); // Added for caret visibility
        } else if (component instanceof JLabel) {
            component.setForeground(colors.get("Label.foreground"));
        } else if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            comboBox.setBackground(colors.get("ComboBox.background"));
            comboBox.setForeground(colors.get("ComboBox.foreground"));
            comboBox.setOpaque(true);
            // Set custom renderer to ensure readable text
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    c.setBackground(isSelected ? colors.get("ComboBox.selectionBackground") : colors.get("ComboBox.background"));
                    c.setForeground(isSelected ? colors.get("ComboBox.selectionForeground") : colors.get("ComboBox.foreground"));
                    return c;
                }
            });
        }
        SwingUtilities.updateComponentTreeUI(component);
        component.repaint();
        component.revalidate();
    }

    public static void applyThemeToOptionPane(JOptionPane optionPane) {
        Map<String, Color> colors = (currentTheme == ThemeMode.LIGHT) ? LIGHT_COLORS : DARK_COLORS;
        optionPane.setBackground(colors.get("OptionPane.background"));
        optionPane.setForeground(colors.get("OptionPane.foreground"));
        for (Component comp : optionPane.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(colors.get("OptionPane.background"));
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JButton) {
                        subComp.setBackground(colors.get("Button.background"));
                        subComp.setForeground(colors.get("Button.foreground"));
                    } else if (subComp instanceof JLabel) {
                        subComp.setForeground(colors.get("OptionPane.foreground"));
                    }
                }
            }
        }
    }

    public interface ThemeChangeListener {
        void onThemeChanged(ThemeMode newTheme);
    }
}
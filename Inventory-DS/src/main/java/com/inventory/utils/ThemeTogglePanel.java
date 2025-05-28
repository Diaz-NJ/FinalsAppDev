package main.java.com.inventory.utils;

import javax.swing.*;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ThemeTogglePanel extends JPanel implements ThemeManager.ThemeChangeListener {
    private JButton toggleButton;

    public ThemeTogglePanel() {
        setLayout(new java.awt.FlowLayout());

        toggleButton = new JButton("Toggle Theme");
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Toggle between light and dark mode
                ThemeManager.ThemeMode newTheme = ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT
                        ? ThemeManager.ThemeMode.DARK
                        : ThemeManager.ThemeMode.LIGHT;
                ThemeManager.setTheme(newTheme);
                updateButtonText();
            }
        });
        add(toggleButton);

        // Register this panel as a theme change listener
        ThemeManager.addThemeChangeListener(this);
        // Apply initial theme
        onThemeChanged(ThemeManager.getCurrentTheme());
    }

    @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        updateButtonText();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
            window.revalidate();
        }
    }

    private void updateButtonText() {
        toggleButton.setText("Switch to " + (ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ? "Dark" : "Light") + " Mode");
    }
}
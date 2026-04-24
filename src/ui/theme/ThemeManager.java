package ui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }

    private static final ThemeManager INSTANCE = new ThemeManager();
    private Theme currentTheme = Theme.LIGHT;
    private final List<Runnable> listeners = new ArrayList<>();
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private String lastScreen = "dashboard";

    private ThemeManager() {
        String saved = prefs.get("theme", "LIGHT");
        currentTheme = "DARK".equals(saved) ? Theme.DARK : Theme.LIGHT;
        lastScreen = prefs.get("lastScreen", "dashboard");
    }

    public static ThemeManager getInstance() { return INSTANCE; }
    public Theme getCurrentTheme() { return currentTheme; }
    public boolean isDark() { return currentTheme == Theme.DARK; }

    public void toggleTheme() {
        currentTheme = isDark() ? Theme.LIGHT : Theme.DARK;
        prefs.put("theme", currentTheme.name());
        notifyListeners();
    }

    public String getLastScreen() { return lastScreen; }
    public void setLastScreen(String screen) {
        this.lastScreen = screen;
        prefs.put("lastScreen", screen);
    }

    public void addListener(Runnable listener) { listeners.add(listener); }
    public void removeListener(Runnable listener) { listeners.remove(listener); }
    private void notifyListeners() { for (Runnable r : listeners) r.run(); }

    // ── Surface Colors ──────────────────────────────────────────────────────
    public Color getBackground()       { return isDark() ? new Color(17, 24, 39)    : new Color(248, 250, 252); }
    public Color getSidebarBg()         { return isDark() ? new Color(15, 23, 42)    : new Color(30, 41, 59);   }
    public Color getSidebarText()       { return new Color(203, 213, 225); }
    public Color getSidebarActive()     { return new Color(59, 130, 246); }
    public Color getNavbarBg()          { return isDark() ? new Color(17, 24, 39)    : Color.WHITE;             }
    public Color getNavbarBorder()      { return isDark() ? new Color(55, 65, 81)    : new Color(229, 231, 235); }

    // ── Card Colors ─────────────────────────────────────────────────────────
    public Color getCardBg()            { return isDark() ? new Color(31, 41, 55)    : Color.WHITE;             }
    public Color getCardBorder()        { return isDark() ? new Color(55, 65, 81)    : new Color(229, 231, 235); }
    public Color getCardInnerBg()       { return isDark() ? new Color(17, 24, 39)    : new Color(249, 250, 251); }

    // ── Text Colors ─────────────────────────────────────────────────────────
    public Color getTextPrimary()       { return isDark() ? new Color(243, 244, 246) : new Color(17, 24, 39);   }
    public Color getTextSecondary()     { return isDark() ? new Color(156, 163, 175) : new Color(107, 114, 128); }
    public Color getTextMuted()         { return isDark() ? new Color(107, 114, 128) : new Color(156, 163, 175); }
    public Color getLabelColor()        { return isDark() ? new Color(229, 231, 235) : new Color(55, 65, 81);   }

    // ── Input Colors ────────────────────────────────────────────────────────
    public Color getInputBg()           { return isDark() ? new Color(31, 41, 55)    : Color.WHITE;             }
    public Color getInputBorder()       { return isDark() ? new Color(55, 65, 81)    : new Color(209, 213, 219); }
    public Color getInputFocusBorder()  { return new Color(59, 130, 246); }
    public Color getInputText()         { return isDark() ? new Color(249, 250, 251) : new Color(17, 24, 39);   }
    public Color getPlaceholderText()   { return new Color(156, 163, 175); }

    // ── Button Colors ───────────────────────────────────────────────────────
    public Color getPrimary()           { return new Color(59, 130, 246);  }
    public Color getPrimaryHover()      { return new Color(37, 99, 235);   }
    public Color getPrimaryPressed()    { return new Color(29, 78, 216);   }
    public Color getDisabledBg()        { return isDark() ? new Color(55, 65, 81)    : new Color(229, 231, 235); }
    public Color getDisabledText()      { return new Color(156, 163, 175); }

    // ── Status Colors ───────────────────────────────────────────────────────
    public Color getSuccess()           { return new Color(34, 197, 94);   }
    public Color getError()             { return new Color(239, 68, 68);   }
    public Color getInfo()              { return new Color(59, 130, 246);  }

    // ── Preview/Text area ───────────────────────────────────────────────────
    public Color getPreviewBg()         { return isDark() ? new Color(17, 24, 39)    : new Color(249, 250, 251); }

    // ── Fonts ───────────────────────────────────────────────────────────────
    public Font getHeaderFont()   { return new Font("Segoe UI", Font.BOLD, 24);   }
    public Font getTitleFont()    { return new Font("Segoe UI", Font.BOLD, 18);   }
    public Font getLabelFont()    { return new Font("Segoe UI", Font.BOLD, 14);   }
    public Font getBodyFont()     { return new Font("Segoe UI", Font.PLAIN, 14);  }
    public Font getSmallFont()    { return new Font("Segoe UI", Font.PLAIN, 12);  }
    public Font getButtonFont()   { return new Font("Segoe UI", Font.BOLD, 14);   }
    public Font getSidebarFont()  { return new Font("Segoe UI", Font.PLAIN, 15);  }
    public Font getNavbarFont()   { return new Font("Segoe UI", Font.BOLD, 16);   }
    public Font getMonoFont()     { return new Font("Consolas", Font.PLAIN, 13);  }

    // ── Recursive Theme Application ─────────────────────────────────────────

    /** Recursively apply theme colors to a component tree. */
    public void applyThemeToTree(Component comp) {
        if (comp == null) return;

        if (comp instanceof JLabel) {
            comp.setForeground(getTextPrimary());
        } else if (comp instanceof JTextField) {
            comp.setBackground(getInputBg());
            comp.setForeground(getInputText());
            ((JTextField) comp).setCaretColor(getInputText());
        } else if (comp instanceof JTextArea) {
            comp.setBackground(getPreviewBg());
            comp.setForeground(getInputText());
            ((JTextArea) comp).setCaretColor(getInputText());
        } else if (comp instanceof JComboBox) {
            styleComboBox((JComboBox<?>) comp);
        } else if (comp instanceof JList) {
            comp.setBackground(getCardBg());
            comp.setForeground(getTextPrimary());
        } else if (comp instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) comp;
            sp.setBorder(BorderFactory.createLineBorder(getCardBorder()));
            sp.getViewport().setBackground(getCardBg());
            JViewport vp = sp.getViewport();
            if (vp != null && vp.getView() != null) {
                applyThemeToTree(vp.getView());
            }
            return; // don't recurse into scrollpane children normally
        } else if (comp instanceof JPanel) {
            // Don't override opaque=false panels (like CardPanel which paints its own bg)
            if (comp.isOpaque()) {
                comp.setBackground(getBackground());
            }
        }

        if (comp instanceof Container) {
            Container c = (Container) comp;
            for (int i = 0; i < c.getComponentCount(); i++) {
                applyThemeToTree(c.getComponent(i));
            }
        }
    }

    /** Style a JComboBox for the current theme with a proper dark-mode renderer. */
    public void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(getInputBg());
        combo.setForeground(getInputText());
        combo.setFont(getBodyFont());
        combo.setBorder(BorderFactory.createLineBorder(getInputBorder()));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ThemeManager t = ThemeManager.getInstance();
                if (isSelected) {
                    setBackground(t.getPrimary());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(t.getInputBg());
                    setForeground(t.getInputText());
                }
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                setFont(t.getBodyFont());
                return this;
            }
        });
    }
}

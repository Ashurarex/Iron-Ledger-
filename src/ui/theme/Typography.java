package ui.theme;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class Typography {
    public static final String FONT_FAMILY = resolveFamily("Segoe UI", "SansSerif");

    public static final Font H1 = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font H2 = semiBold(18);
    public static final Font BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font NAV = semiBold(16);
    public static final Font MONO = new Font(resolveFamily("Consolas", "Monospaced"), Font.PLAIN, 13);

    private Typography() {
    }

    public static void applyTypography(Component component) {
        if (component == null) {
            return;
        }

        applyTypographyToComponent(component);

        if (component instanceof Container) {
            Container container = (Container) component;
            for (int i = 0; i < container.getComponentCount(); i++) {
                applyTypography(container.getComponent(i));
            }
        }
    }

    public static void applyTypographyToComponent(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof JLabel) {
            component.setFont(resolveLabelFont(component.getFont()));
        } else if (component instanceof AbstractButton) {
            component.setFont(BODY_BOLD);
        } else if (component instanceof JTextField) {
            component.setFont(BODY);
        } else if (component instanceof JTextArea) {
            component.setFont(BODY);
        }
    }

    private static Font resolveLabelFont(Font current) {
        if (current == null) {
            return BODY;
        }
        if (current.getSize() >= 22) {
            return H1;
        }
        if (current.getSize() >= 17) {
            return H2;
        }
        if (current.getSize() <= 12) {
            return SMALL;
        }
        return BODY;
    }

    private static Font semiBold(int size) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FAMILY, FONT_FAMILY);
        attributes.put(TextAttribute.SIZE, (float) size);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
        return new Font(attributes);
    }

    private static String resolveFamily(String preferred, String fallback) {
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String family : families) {
            if (family.equalsIgnoreCase(preferred)) {
                return family;
            }
        }
        return fallback;
    }
}

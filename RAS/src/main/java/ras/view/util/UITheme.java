package ras.view.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UITheme {

    // Color Palette — Dark premium theme
    public static final Color BG_DARK        = new Color(18, 18, 30);
    public static final Color BG_PANEL       = new Color(28, 28, 45);
    public static final Color BG_CARD        = new Color(36, 36, 58);
    public static final Color BG_INPUT       = new Color(44, 44, 68);
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241);   // Indigo
    public static final Color ACCENT_SUCCESS = new Color(34, 197, 94);    // Green
    public static final Color ACCENT_WARNING = new Color(251, 191, 36);   // Amber
    public static final Color ACCENT_DANGER  = new Color(239, 68, 68);    // Red
    public static final Color ACCENT_INFO    = new Color(56, 189, 248);   // Sky blue
    public static final Color TEXT_PRIMARY   = new Color(241, 241, 255);
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    public static final Color TEXT_MUTED     = new Color(100, 116, 139);
    public static final Color BORDER_COLOR   = new Color(55, 55, 85);
    public static final Color TABLE_ROW_ALT  = new Color(32, 32, 52);
    public static final Color TABLE_SELECT   = new Color(79, 82, 180);

    // Fonts
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD, 13);

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT_PRIMARY);
        UIManager.put("PasswordField.background", BG_INPUT);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", TABLE_SELECT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("TableHeader.background", BG_PANEL);
        UIManager.put("TableHeader.foreground", ACCENT_PRIMARY);
        UIManager.put("ScrollPane.background", BG_PANEL);
        UIManager.put("ScrollBar.background", BG_PANEL);
        UIManager.put("ScrollBar.thumb", BG_CARD);
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
    }

    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bg;
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    public static JTextField createTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(FONT_BODY);
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return pf;
    }

    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        return cb;
    }

    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_SUBTITLE);
            lbl.setForeground(ACCENT_PRIMARY);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            card.add(lbl, BorderLayout.NORTH);
        }
        return card;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(TABLE_SELECT);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(28);
        table.setFont(FONT_BODY);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        // Alternate row coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (sel) {
                    setBackground(TABLE_SELECT);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? BG_CARD : TABLE_ROW_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_PANEL);
        header.setForeground(ACCENT_PRIMARY);
        header.setFont(FONT_SUBTITLE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PRIMARY));
    }

    public static JScrollPane scrollPane(Component comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

    public static JPanel statCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 2),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(accent);
        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(FONT_SMALL);
        nameLbl.setForeground(TEXT_SECONDARY);
        card.add(valLbl);
        card.add(nameLbl);
        return card;
    }
}

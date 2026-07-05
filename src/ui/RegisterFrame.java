package ui;

import database.MahasiswaDAO;
import model.Mahasiswa;
import ui.components.GradientButton;
import ui.components.RoundedTextField;
import ui.components.RoundedPasswordField;
import util.AppIcon;
import util.ColorPalette;

import javax.swing.*;
import java.awt.*;

/**
 */
public class RegisterFrame extends JFrame {

    private RoundedTextField namaField;
    private RoundedTextField nimField;
    private RoundedPasswordField passwordField;
    private JLabel errorLabel;

    public RegisterFrame() {
        // Register screen selalu light mode
        ColorPalette.isDarkMode = false;

        setTitle("TaskU — Daftar Akun");
        AppIcon.apply(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel (gradient background)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(235, 240, 255),
                    0, getHeight(), new Color(240, 250, 255)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        // Card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(340, 430));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        gbc.gridy = 0;
        gbc.insets = new Insets(25, 40, 0, 40);
        JLabel logo = new JLabel("TaskU") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = fm.getAscent();
                GradientPaint gp = new GradientPaint(
                    x, 0, ColorPalette.ACCENT_BLUE,
                    x + fm.stringWidth(text), 0, ColorPalette.ACCENT_CYAN
                );
                g2.setPaint(gp);
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        logo.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 32));
        logo.setPreferredSize(new Dimension(260, 40));
        card.add(logo, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 40, 0, 40);
        JLabel title = new JLabel("Buat Akun Baru", SwingConstants.CENTER);
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 16));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        card.add(title, gbc);

        // Nama
        gbc.gridy = 2;
        gbc.insets = new Insets(16, 40, 0, 40);
        card.add(makeLabel("Username"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 40, 0, 40);
        namaField = new RoundedTextField("");
        namaField.setPreferredSize(new Dimension(260, 36));
        card.add(namaField, gbc);

        // NIM
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 40, 0, 40);
        card.add(makeLabel("NIM"), gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 40, 0, 40);
        nimField = new RoundedTextField("");
        nimField.setPreferredSize(new Dimension(260, 36));
        card.add(nimField, gbc);

        // Password
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 40, 0, 40);
        card.add(makeLabel("Password"), gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(4, 40, 0, 40);
        passwordField = new RoundedPasswordField("");
        passwordField.setPreferredSize(new Dimension(260, 36));
        card.add(passwordField, gbc);

        // Error label
        gbc.gridy = 8;
        gbc.insets = new Insets(6, 40, 0, 40);
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ColorPalette.FONT_SMALL);
        errorLabel.setForeground(ColorPalette.PRIORITY_HIGH);
        card.add(errorLabel, gbc);

        // Daftar Button (gradient)
        gbc.gridy = 9;
        gbc.insets = new Insets(8, 40, 0, 40);
        GradientButton registerBtn = new GradientButton("Daftar");
        registerBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 14));
        registerBtn.setPreferredSize(new Dimension(260, 40));
        registerBtn.addActionListener(e -> doRegister());
        card.add(registerBtn, gbc);

        // Back to Login link
        gbc.gridy = 10;
        gbc.insets = new Insets(14, 40, 25, 40);
        JLabel backLink = new JLabel("Sudah punya akun? Masuk di sini", SwingConstants.CENTER);
        backLink.setFont(ColorPalette.FONT_SMALL);
        backLink.setForeground(ColorPalette.ACCENT_BLUE);
        backLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });
        card.add(backLink, gbc);

        mainPanel.add(card);
        passwordField.addActionListener(e -> doRegister());
    }

    private void doRegister() {
        String nama = namaField.getText().trim();
        String nim = nimField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (nama.isEmpty() || nim.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Semua field wajib diisi!");
            return;
        }
        if (password.length() < 4) {
            errorLabel.setText("Password minimal 4 karakter!");
            return;
        }

        MahasiswaDAO dao = new MahasiswaDAO();
        if (dao.isNimExists(nim)) {
            errorLabel.setText("NIM sudah terdaftar! Silakan login.");
            return;
        }

        Mahasiswa newUser = new Mahasiswa(nama, nim);
        newUser.setPassword(password);

        if (dao.register(newUser)) {
            errorLabel.setForeground(new Color(16, 130, 70));
            errorLabel.setText("✓ Registrasi berhasil! Mengalihkan...");
            // Auto-redirect ke login setelah 1.5 detik
            javax.swing.Timer timer = new javax.swing.Timer(1500, ev -> {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            errorLabel.setText("Registrasi gagal. Coba lagi.");
        }
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ColorPalette.FONT_BUTTON);
        l.setForeground(ColorPalette.TEXT_PRIMARY);
        return l;
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(ColorPalette.FONT_BODY);
        f.setPreferredSize(new Dimension(260, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
    }
}

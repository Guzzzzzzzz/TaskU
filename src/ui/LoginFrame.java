package ui;

import database.MahasiswaDAO;
import model.Mahasiswa;
import ui.components.RoundedTextField;
import ui.components.RoundedPasswordField;
import ui.components.GradientButton;
import util.AppIcon;
import util.ColorPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 *  Fitur UI:
 *  - Rounded input fields dengan placeholder text
 *  - Focus highlight (border biru saat active)
 *  - Link navigasi: "Lupa password?" (kiri) dan "Daftar" (kanan)
 *
 *              Polymorphism (override paintComponent)
 */
public class LoginFrame extends JFrame {

    private RoundedTextField usernameField;
    private RoundedPasswordField passwordField;
    private JLabel errorLabel;


    public LoginFrame() {
        // Login screen selalu light mode — reset agar komponen input tidak inherit dark mode dari sesi sebelumnya
        ColorPalette.isDarkMode = false;

        setTitle("TaskU — Login");
        AppIcon.apply(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel (gradient background)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        // Card Panel
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 24, 24);
                // Card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 24, 24);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 430));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo "TaskU" dengan gradient
        gbc.gridy = 0;
        gbc.insets = new Insets(32, 44, 0, 44);
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
        logo.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 36));
        logo.setPreferredSize(new Dimension(260, 44));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logo, gbc);

        // Title + Subtitle
        gbc.gridy = 1;
        gbc.insets = new Insets(16, 44, 0, 44);
        JLabel title = new JLabel("Selamat Datang!", SwingConstants.CENTER);
        title.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 18));
        title.setForeground(ColorPalette.TEXT_PRIMARY);
        card.add(title, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(4, 44, 0, 44);
        JLabel subtitle = new JLabel("Masukkan Username dan Password untuk login", SwingConstants.CENTER);
        subtitle.setFont(ColorPalette.FONT_SMALL);
        subtitle.setForeground(ColorPalette.TEXT_SECONDARY);
        card.add(subtitle, gbc);

        // Username Field (rounded + placeholder)
        gbc.gridy = 3;
        gbc.insets = new Insets(24, 44, 0, 44);
        usernameField = new RoundedTextField("Masukkan Username");
        card.add(usernameField, gbc);

        // Password Field (rounded + placeholder)
        gbc.gridy = 4;
        gbc.insets = new Insets(14, 44, 0, 44);
        passwordField = new RoundedPasswordField("Masukkan Password");
        card.add(passwordField, gbc);

        // Lupa password? (rata kanan, di bawah password field)
        gbc.gridy = 5;
        gbc.insets = new Insets(6, 44, 0, 44);
        JLabel forgotLink = new JLabel("Lupa password?");
        forgotLink.setFont(ColorPalette.FONT_SMALL);
        forgotLink.setForeground(ColorPalette.TEXT_SECONDARY);
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLink.setHorizontalAlignment(SwingConstants.RIGHT);
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLink.setForeground(ColorPalette.ACCENT_BLUE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                forgotLink.setForeground(ColorPalette.TEXT_SECONDARY);
            }
        });
        card.add(forgotLink, gbc);

        // Error Label
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 44, 0, 44);
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ColorPalette.FONT_SMALL);
        errorLabel.setForeground(ColorPalette.PRIORITY_HIGH);
        card.add(errorLabel, gbc);

        // Masuk Button (gradient)
        gbc.gridy = 7;
        gbc.insets = new Insets(6, 44, 0, 44);
        GradientButton loginBtn = new GradientButton("Masuk");
        loginBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(260, 42));
        loginBtn.addActionListener(e -> doLogin());
        card.add(loginBtn, gbc);

        // "Belum punya akun? Daftar sekarang" (tengah bawah)
        gbc.gridy = 8;
        gbc.insets = new Insets(18, 44, 28, 44);
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        registerRow.setOpaque(false);

        JLabel registerText = new JLabel("Belum punya akun? ");
        registerText.setFont(ColorPalette.FONT_SMALL);
        registerText.setForeground(ColorPalette.TEXT_SECONDARY);
        registerRow.add(registerText);

        JLabel registerLink = new JLabel("Daftar sekarang");
        registerLink.setFont(new Font(ColorPalette.FONT_SMALL.getFamily(), Font.BOLD, ColorPalette.FONT_SMALL.getSize()));
        registerLink.setForeground(ColorPalette.ACCENT_BLUE);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                registerLink.setForeground(ColorPalette.ACCENT_CYAN);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                registerLink.setForeground(ColorPalette.ACCENT_BLUE);
            }
        });
        registerRow.add(registerLink);

        card.add(registerRow, gbc);

        mainPanel.add(card);

        // Enter key shortcuts
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }


    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username dan Password tidak boleh kosong!");
            return;
        }

        MahasiswaDAO dao = new MahasiswaDAO();
        Mahasiswa loggedIn = dao.loginByUsername(username, password);

        if (loggedIn != null) {
            System.out.println("[Login] Berhasil: " + loggedIn.getNama() + " (" + loggedIn.getNim() + ")");
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(loggedIn).setVisible(true));
        } else {
            errorLabel.setText("Username atau Password salah!");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }


    private void showForgotPasswordDialog() {
        // ── Dialog utama (undecorated, rounded) ──
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 22, 22);
                // Card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 22, 22);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new javax.swing.border.EmptyBorder(28, 32, 24, 32));

        // Title
        JLabel titleLabel = new JLabel("Reset Password");
        titleLabel.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 18));
        titleLabel.setForeground(ColorPalette.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));

        // Subtitle
        JLabel subLabel = new JLabel("Masukkan NIM untuk mereset password Anda");
        subLabel.setFont(ColorPalette.FONT_SMALL);
        subLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(subLabel);
        content.add(Box.createVerticalStrut(18));

        // NIM Input (rounded)
        RoundedTextField nimInput = new RoundedTextField("Masukkan NIM");
        nimInput.setMaximumSize(new Dimension(260, 40));
        nimInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(nimInput);
        content.add(Box.createVerticalStrut(8));

        // Result label (untuk menampilkan status/error)
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(ColorPalette.FONT_SMALL);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(resultLabel);

        // Password container (untuk menampilkan password baru yang bisa di-copy)
        JPanel passContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        passContainer.setOpaque(false);
        passContainer.setVisible(false);
        passContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabelText = new JLabel("Password baru: ");
        passLabelText.setFont(ColorPalette.FONT_SMALL);
        passLabelText.setForeground(new Color(16, 130, 70));

        JTextField passValueField = new JTextField(8);
        passValueField.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        passValueField.setEditable(false);
        passValueField.setBorder(null);
        passValueField.setOpaque(false);
        passValueField.setForeground(new Color(16, 130, 70));
        passValueField.setSelectionColor(new Color(16, 130, 70, 40));
        passValueField.setSelectedTextColor(new Color(16, 130, 70));

        JButton copyBtn = new JButton("Salin") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        copyBtn.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 10));
        copyBtn.setBackground(new Color(230, 245, 235));
        copyBtn.setForeground(new Color(16, 130, 70));
        copyBtn.setBorderPainted(false);
        copyBtn.setContentAreaFilled(false);
        copyBtn.setFocusPainted(false);
        copyBtn.setOpaque(false);
        copyBtn.setPreferredSize(new Dimension(54, 22));
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        copyBtn.addActionListener(e -> {
            String text = passValueField.getText();
            if (!text.isEmpty()) {
                java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(text);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                copyBtn.setText("Tersalin");
                Timer t = new Timer(2000, ev -> copyBtn.setText("Salin"));
                t.setRepeats(false);
                t.start();
            }
        });

        passContainer.add(passLabelText);
        passContainer.add(passValueField);
        passContainer.add(copyBtn);
        content.add(passContainer);
        content.add(Box.createVerticalStrut(4));

        // Info label (muncul setelah reset berhasil)
        JLabel infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("Plus Jakarta Sans", Font.PLAIN, 10));
        infoLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(infoLabel);
        content.add(Box.createVerticalStrut(16));

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        // Tombol Reset
        JButton resetBtn = new JButton("Reset") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        resetBtn.setFont(ColorPalette.FONT_BUTTON);
        resetBtn.setBackground(ColorPalette.ACCENT_BLUE);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setOpaque(false);
        resetBtn.setContentAreaFilled(false);
        resetBtn.setBorderPainted(false);
        resetBtn.setFocusPainted(false);
        resetBtn.setPreferredSize(new Dimension(90, 34));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            String nim = nimInput.getText().trim();
            if (nim.isEmpty()) {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("NIM tidak boleh kosong!");
                passContainer.setVisible(false);
                infoLabel.setText(" ");
                dialog.pack();
                dialog.setLocationRelativeTo(LoginFrame.this);
                return;
            }
            MahasiswaDAO dao = new MahasiswaDAO();
            if (!dao.isNimExists(nim)) {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("NIM tidak ditemukan!");
                passContainer.setVisible(false);
                infoLabel.setText(" ");
                dialog.pack();
                dialog.setLocationRelativeTo(LoginFrame.this);
                return;
            }

            // Generate temporary password (8 karakter alfanumerik)
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
            StringBuilder sb = new StringBuilder();
            java.security.SecureRandom rng = new java.security.SecureRandom();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(rng.nextInt(chars.length())));
            }
            String tempPassword = sb.toString();

            // Hash dan simpan ke database
            String hashedTemp = util.PasswordUtil.hash(tempPassword);
            if (dao.resetPasswordByNim(nim, hashedTemp)) {
                resultLabel.setText(" ");
                passValueField.setText(tempPassword);
                passContainer.setVisible(true);
                infoLabel.setText("Segera login dan ganti password Anda!");
                
                // Salin otomatis ke clipboard untuk kenyamanan maksimal
                try {
                    java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(tempPassword);
                    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                    copyBtn.setText("Tersalin");
                    Timer t = new Timer(2000, ev -> copyBtn.setText("Salin"));
                    t.setRepeats(false);
                    t.start();
                } catch (Exception ex) {
                    System.err.println("Gagal menyalin ke clipboard secara otomatis: " + ex.getMessage());
                }

                // Disable tombol reset agar tidak di-klik berkali-kali
                resetBtn.setEnabled(false);
                nimInput.setEnabled(false);
                
                dialog.pack();
                dialog.setLocationRelativeTo(LoginFrame.this);
                dialog.revalidate();
                dialog.repaint();
            } else {
                resultLabel.setForeground(ColorPalette.PRIORITY_HIGH);
                resultLabel.setText("Gagal mereset password!");
                passContainer.setVisible(false);
                infoLabel.setText(" ");
                dialog.pack();
                dialog.setLocationRelativeTo(LoginFrame.this);
            }
        });


        // Tombol Tutup
        JButton tutupBtn = new JButton("Tutup") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ColorPalette.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(ColorPalette.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.setColor(ColorPalette.TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        tutupBtn.setFont(ColorPalette.FONT_BUTTON);
        tutupBtn.setOpaque(false);
        tutupBtn.setContentAreaFilled(false);
        tutupBtn.setBorderPainted(false);
        tutupBtn.setFocusPainted(false);
        tutupBtn.setPreferredSize(new Dimension(90, 34));
        tutupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tutupBtn.addActionListener(e -> dialog.dispose());

        btnRow.add(tutupBtn);
        btnRow.add(resetBtn);
        content.add(btnRow);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class AplikasiKeuanganPribadiFrame extends javax.swing.JFrame {
          private DefaultTableModel tableModel;
          private int editingId = -1;
    /**
     * Creates new form AplikasiKeuanganPribadiFrame
     */
    public AplikasiKeuanganPribadiFrame() {
        initComponents();
          buatDatabase(); // Membuat database dan tabel jika belum ada
         initTable();    // Inisialisasi tabel dan load data dari database
        cekDanTambahkanKolomSaldo(); // Pastikan kolom 'saldo' ada
        
        // Tambahkan item ke ComboBox kategori
         jComboBox1.addItem("Harian");
         jComboBox1.addItem("Bulanan");
         jComboBox1.addItem("Tahunan");
         
         
          // Tambahkan item ke ComboBox kategori
         jComboBox2.addItem("Masuk");
         jComboBox2.addItem("Keluar");
         

    
    jButton3.setText("EDIT");
    jButton3.addActionListener(e -> editData());
    
    jButton5.setText("UPDATE");
    jButton5.addActionListener(e -> simpanPerubahan());
    
    jButton2.setText("Lihat Tabungan");
    jButton2.addActionListener(e -> {
    double totalTabungan = hitungTabungan();
    JOptionPane.showMessageDialog(this, "Total Tabungan Anda: Rp. " + String.format("%,.0f", totalTabungan), 
                                  "Informasi Tabungan", JOptionPane.INFORMATION_MESSAGE);
});
    } 
        
        // Mengecek dan menambahkan kolom saldo ke tabel database
    private void cekDanTambahkanKolomSaldo() {
        String url = "jdbc:sqlite:data.db";
        String alterTableSql = "ALTER TABLE data ADD COLUMN saldo REAL DEFAULT 0";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(alterTableSql); // Coba tambahkan kolom saldo
            System.out.println("Kolom 'saldo' berhasil ditambahkan.");
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate column name")) {
                System.out.println("Kolom 'saldo' sudah ada, tidak perlu ditambahkan.");
            } else {
                JOptionPane.showMessageDialog(this, "Error menambahkan kolom 'saldo': " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

   private void tambahData() {
    // Mengambil data dari inputan form
    String deskripsi = jTextField1.getText();

    // Validasi deskripsi tidak boleh kosong
    if (deskripsi.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Deskripsi tidak boleh kosong!");
        return;
    }

    Date tanggal = jDateChooser1.getDate();
    if (tanggal == null) {
        JOptionPane.showMessageDialog(this, "Tanggal tidak boleh kosong!");
        return;
    }

    double jumlah = 0;
    try {
        jumlah = Double.parseDouble(jTextField2.getText().replace(",", "").replace("Rp", "").trim());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!");
        return;
    }

    String kategori = jComboBox1.getSelectedItem() != null ? jComboBox1.getSelectedItem().toString() : "";
    String jenisTransaksi = jComboBox2.getSelectedItem() != null ? jComboBox2.getSelectedItem().toString() : "";

    if (kategori.isEmpty() || jenisTransaksi.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Kategori dan jenis transaksi tidak boleh kosong!");
        return;
    }

    // Format tanggal menjadi string yang lebih terstruktur
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String formattedTanggal = dateFormat.format(tanggal);

    // Format jumlah ke format mata uang
    DecimalFormat rupiahFormat = new DecimalFormat("Rp #,##0");
    String formattedJumlah = rupiahFormat.format(jumlah);

    // Menambahkan data ke tabel
    int id = generateId();
tableModel.addRow(new Object[]{id, deskripsi, formattedTanggal, formattedJumlah, kategori, jenisTransaksi});


    // Update saldo setelah data ditambahkan
    updateJumlah(jenisTransaksi, jumlah);

    // Menyimpan data transaksi ke database (jika ada)
    saveData(deskripsi, tanggal, jumlah, kategori, jenisTransaksi);

    // Mengosongkan form setelah data ditambahkan
    clearForm();
}



private void clearForm() {
    // Clear semua form input setelah data ditambahkan
    jTextField1.setText("");
    jTextField2.setText("");
    jComboBox1.setSelectedIndex(0); // Reset kategori ke index pertama
    jComboBox2.setSelectedIndex(0); // Reset jenis transaksi ke index pertama
    jDateChooser1.setDate(null); // Reset tanggal
}

private int generateId() {
    // Fungsi untuk menghasilkan ID baru, misalnya auto-increment
    // Asumsikan ID berurutan, atau Anda bisa menggunakan sistem ID lain
    int newId = tableModel.getRowCount() + 1; // ID baru berdasarkan jumlah baris tabel
    return newId;
}

private void editData() {
    // Ambil baris yang dipilih di tabel
    int selectedRow = jTable1.getSelectedRow();

    // Cek jika tidak ada baris yang dipilih
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit terlebih dahulu!", "Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Ambil data dari baris yang dipilih
    int id = (int) tableModel.getValueAt(selectedRow, 0); // Kolom 0 adalah ID
    String deskripsi = (String) tableModel.getValueAt(selectedRow, 1);
    String tanggal = (String) tableModel.getValueAt(selectedRow, 2);
    double jumlah = 0;

    try {
        // Pastikan format jumlah yang ada di tabel benar (misal: Rp 250.000 -> 250000)
        String jumlahString = (String) tableModel.getValueAt(selectedRow, 3);
        jumlah = Double.parseDouble(jumlahString.replaceAll("[^\\d.-]", "")); // Menghapus simbol dan memparsing sebagai double
    } catch (Exception e) {
        // Jika terjadi error dalam parsing jumlah, tampilkan pesan error
        JOptionPane.showMessageDialog(this, "Format jumlah tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String kategori = (String) tableModel.getValueAt(selectedRow, 4);
    String jenisTransaksi = (String) tableModel.getValueAt(selectedRow, 5);

    // Tampilkan data di komponen input untuk diedit
    jTextField1.setText(deskripsi);
    try {
        jDateChooser1.setDate(java.sql.Date.valueOf(tanggal)); // Tampilkan tanggal di DateChooser
    } catch (IllegalArgumentException e) {
        jDateChooser1.setDate(null); // Jika format tanggal tidak valid, biarkan kosong
    }
    jComboBox1.setSelectedItem(kategori); // Pilih kategori
    jComboBox2.setSelectedItem(jenisTransaksi); // Pilih jenis transaksi

    // Simpan ID baris yang sedang diedit ke variabel global
    this.editingId = id;  // Variabel global untuk ID transaksi yang sedang diedit

}
   
  private void simpanPerubahan() {
    if (editingId == -1) {
        JOptionPane.showMessageDialog(this, "Tidak ada data yang sedang diedit.", "Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        // Ambil data dari form input
        String deskripsi = jTextField1.getText();
        Date tanggal = jDateChooser1.getDate();
        double jumlah = Double.parseDouble(jTextField2.getText().replace("Rp. ", "").replace(",", ""));
        String kategori = (String) jComboBox1.getSelectedItem();
        String jenisTransaksi = (String) jComboBox2.getSelectedItem();

        // Simpan perubahan ke database
        updateData(editingId, deskripsi, tanggal, jumlah, kategori, jenisTransaksi);

        // Reset form input dan ID editing
        resetForm();
        editingId = -1;

        // Refresh tabel
        loadData();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Simpan Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void resetForm() {
    jTextField1.setText("");
    jDateChooser1.setDate(null);
    jTextField2.setText("Rp. 0");
    jComboBox1.setSelectedIndex(0);
    jComboBox2.setSelectedIndex(0);
}

// Fungsi untuk memperbarui dan memformat nilai yang dimasukkan
private void updateJumlah(String jenisTransaksi, double jumlah) {
    try {
        // Menghapus "Rp." dan titik untuk konversi ke angka
        String text = jTextField2.getText().replace("Rp. ", "").replace(".", "");

        // Cek apakah text bukan kosong
        if (!text.isEmpty()) {
            double value = Double.parseDouble(text);

            // Format ulang jumlah yang dimasukkan dengan simbol "Rp." di depan angka
            DecimalFormat format = new DecimalFormat("Rp."); // Format dengan Rp. dan pemisah ribuan
            jTextField2.setText(format.format(value)); // Set format ke JTextField
        } else {
            // Jika kosong, set default Rp. 0
            jTextField2.setText("Rp. ");
        }
    } catch (NumberFormatException ex) {
        // Jika input tidak valid, tetap gunakan format lama
        jTextField2.setText("Rp. ");
    }
}
// Format angka menjadi format Rupiah
private String formatCurrency(double value) {
    DecimalFormat format = new DecimalFormat(""); // Hanya angka
    return "Rp. " + format.format(value); // Tambahkan "Rp." secara manual
}

private double hitungTabungan() {
    String sql = "SELECT SUM(CASE WHEN jenis_transaksi = 'Masuk' THEN jumlah " +
                 "WHEN jenis_transaksi = 'Keluar' THEN -jumlah ELSE 0 END) AS total_tabungan FROM data";
    double totalTabungan = 0.0;

    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        if (rs.next()) {
            totalTabungan = rs.getDouble("total_tabungan");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error menghitung tabungan: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    return totalTabungan;
}
private void tampilkanTabungan() {
    double totalTabungan = hitungTabungan();
    jLabel6.setText("Total Tabungan: Rp. " + String.format("%,.0f", totalTabungan));
}
       // Membuat database dan tabel jika belum ada
    private void buatDatabase() {
    String url = "jdbc:sqlite:data.db";
    String sql = "CREATE TABLE IF NOT EXISTS data ("
               + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
               + "deskripsi TEXT NOT NULL, "
               + "tanggal TEXT NOT NULL, "
               + "jumlah REAL NOT NULL, "
               + "kategori TEXT NOT NULL, "
               + "jenis_transaksi TEXT NOT NULL"
               + ");";

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
        System.out.println("Database dan tabel berhasil dibuat atau sudah ada.");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
    // Inisialisasi tabel dan data
    private void initTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Deskripsi");
        tableModel.addColumn("Tanggal");
        tableModel.addColumn("Jumlah");
        tableModel.addColumn("Kategori");
        tableModel.addColumn("Jenis Transaksi");
        jTable1.setModel(tableModel);
        loadData();
    }
  private void loadData() {
    String sql = "SELECT * FROM data";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        tableModel.setRowCount(0); // Reset data tabel
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                    rs.getInt("id"), // Ambil ID yang otomatis ditangani oleh database
                    rs.getString("deskripsi"),
                    formatDate(rs.getString("tanggal")),
                    formatCurrency(rs.getDouble("jumlah")),
                    rs.getString("kategori"),
                    rs.getString("jenis_transaksi")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
  private void saveData(String deskripsi, Date tanggal, double jumlah, String kategori, String jenisTransaksi) {
    String sql = "INSERT INTO data (deskripsi, tanggal, jumlah, kategori, jenis_transaksi) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, deskripsi);
        pstmt.setString(2, new java.sql.Date(tanggal.getTime()).toString());
        pstmt.setDouble(3, jumlah);
        pstmt.setString(4, kategori);
        pstmt.setString(5, jenisTransaksi);
        pstmt.executeUpdate(); // Database akan menangani ID secara otomatis
        JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Update data berdasarkan ID
    private void updateData(int id, String deskripsi, Date tanggal, double jumlah, String kategori, String jenisTransaksi) {
    String sql = "UPDATE data SET deskripsi = ?, tanggal = ?, jumlah = ?, kategori = ?, jenis_transaksi = ? WHERE id = ?";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, deskripsi);
        pstmt.setString(2, new java.sql.Date(tanggal.getTime()).toString());
        pstmt.setDouble(3, jumlah);
        pstmt.setString(4, kategori);
        pstmt.setString(5, jenisTransaksi);
        pstmt.setInt(6, id);

        pstmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
     // Fungsi untuk menghapus satu baris berdasarkan ID yang dipilih
private void hapusData() {
    int selectedRow = jTable1.getSelectedRow(); // Mendapatkan baris yang dipilih
    if (selectedRow != -1) { // Pastikan ada baris yang dipilih
        int idToDelete = (int) tableModel.getValueAt(selectedRow, 0); // Ambil ID dari kolom pertama

        // Tampilkan konfirmasi sebelum menghapus
        int response = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            // Menghapus data berdasarkan ID dari database
            deleteData(idToDelete); // Panggil fungsi untuk menghapus data dari database
            tableModel.removeRow(selectedRow); // Menghapus baris dari tabel
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
        }
    } else {
        JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.");
    }
}
// Fungsi untuk menghapus data dari database berdasarkan ID
private void deleteData(int id) {
    String sql = "DELETE FROM data WHERE id = ?";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, id); // Set ID yang akan dihapus
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Data dengan ID " + id + " berhasil dihapus.");
        } else {
            System.out.println("Data dengan ID " + id + " tidak ditemukan.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void exportToExcel(){
                                          
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Pilih Lokasi untuk Menyimpan File");

 // Set filter hanya untuk file Excel
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xls, *.xlsx)", "xls", "xlsx"));

    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        // Ambil file yang dipilih
        File fileToSave = fileChooser.getSelectedFile();

        // Tambahkan ekstensi jika tidak ada
        if (!fileToSave.getName().endsWith(".xls") && !fileToSave.getName().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
        }

        try {
            // Panggil metode untuk menyimpan data ke file
            saveToExcel(fileToSave);
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan di " + fileToSave.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menyimpan file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        // Pengguna membatalkan dialog
        JOptionPane.showMessageDialog(this, "Proses ekspor dibatalkan.");
    }
}
private void saveToExcel(File file) throws IOException {
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM data");
         FileWriter writer = new FileWriter(file)) {

        // Tulis header kolom
        writer.write("ID\tDeskripsi\tTanggal\tJumlah\tKategori\tJenis Transaksi\tSaldo\n");

         // Variabel untuk menghitung saldo akhir
        double saldoAkhir = 0.0;
        DecimalFormat rupiahFormat = new DecimalFormat("Rp #,##0.00"); // Format Rp dengan 2 desimal

        // Iterasi melalui hasil query dan tulis ke file
        while (rs.next()) {
            String tanggal = rs.getString("tanggal");
            if (tanggal != null) {
                // Pastikan format tanggal sesuai dengan yang dibutuhkan
                tanggal = formatDate(tanggal); // Format tanggal menjadi format yang dapat dibaca

       // ambil data transaksi
       double jumlah = rs.getDouble("jumlah");
                String jenisTransaksi = rs.getString("jenis_transaksi");
                
                // Hitung saldo berdasarkan jenis transaksi
                if ("Masuk".equalsIgnoreCase(jenisTransaksi)) {
                    saldoAkhir += jumlah;  // Tambah saldo jika transaksi Masuk
                } else if ("Keluar".equalsIgnoreCase(jenisTransaksi)) {
                    saldoAkhir -= jumlah;  // Kurangi saldo jika transaksi Keluar
                }
                
                
                
                
        // Tulis data ke dalam file
             writer.write(
            rs.getInt("id") + "\t" +
            rs.getString("deskripsi") + "\t" +
            tanggal + "\t" +
            rupiahFormat.format(jumlah) + "\t" +  // Format jumlah
            rs.getString("kategori") + "\t" +
            jenisTransaksi + "\t" +
            rupiahFormat.format(saldoAkhir) + "\n"  // Format saldo akhir
        );

            }
        }

        JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke Excel!");

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error membaca database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error menulis file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        throw e; // Lanjutkan exception agar bisa ditangani di tempat lain
    }
}

// Fungsi untuk memformat tanggal (contoh: "2024-11-22" menjadi "22-Nov-2024")
private String formatDate(String date) {
    try {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date parsedDate = inputFormat.parse(date);
        return outputFormat.format(parsedDate);
    } catch (ParseException e) {
        e.printStackTrace();
        return date; // Kembalikan tanggal mentah jika terjadi error
    }

    }       
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(153, 153, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "AplikasiKeuanganPribadi", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jLabel1.setText("Deskripsi :");

        jLabel2.setText("Tanggal :");

        jLabel3.setText("Jumlah :");

        jLabel4.setText("Kategori :");

        jLabel5.setText("Jenis Transaksi :");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        jButton1.setText("TAMBAH");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Lihat tabungan");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton3.setText("EDIT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(255, 51, 51));
        jButton4.setText("MUAT DATA");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("UPDATE");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("HAPUS");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("KELUAR");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jButton3)
                .addGap(66, 66, 66)
                .addComponent(jButton5)
                .addGap(76, 76, 76)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addGap(53, 53, 53))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel2))
                                .addGap(119, 119, 119)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField1)
                                    .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                                    .addComponent(jTextField2)
                                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(226, 226, 226)
                                .addComponent(jLabel6)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addComponent(jLabel6)
                .addGap(37, 37, 37)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(43, 43, 43)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        tambahData();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        editData();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
 
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        hapusData();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        exportToExcel();
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AplikasiKeuanganPribadiFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AplikasiKeuanganPribadiFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AplikasiKeuanganPribadiFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiKeuanganPribadiFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AplikasiKeuanganPribadiFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}

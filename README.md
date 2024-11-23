# AplikasiKeuanganPribadi
 UTS-ilfizahrah-2210010537

---

# Deskripsi

Aplikasi Keuangan Pribadi adalah aplikasi berbasis Java yang dirancang untuk membantu pengguna dalam mencatat, mengelola, dan melacak keuangan pribadi mereka. Aplikasi ini memanfaatkan database untuk menyimpan data transaksi sehingga dapat dikelola dengan mudah dan efisien.
## Fitur Utama
1.	Pencatatan Transaksi Keuangan:
-	Tambahkan transaksi keuangan seperti pemasukan (income) atau pengeluaran (expense).
-	Setiap transaksi mencakup detail seperti ID, deskripsi, tanggal, jumlah uang, kategori, dan jenis transaksi (Masuk atau Keluar).
2.	 Kategori dan Jenis Transaksi:
-	Mendukung berbagai kategori transaksi, seperti "Harian", "Bulanan", "Tahun".
-	Jenis transaksi terbagi menjadi Masuk (pemasukan) dan Keluar (pengeluaran).
3.	Tabel Data Dinamis:
-	Menampilkan semua transaksi dalam tabel yang dapat diperbarui secara real-time.
-	Tabel mencakup kolom seperti ID, Deskripsi, Tanggal, Jumlah, Kategori, Jenis Transaksi, dan Saldo Akhir.
4.	Ekspor ke File Excel:
-	Data transaksi dapat diekspor ke file Excel untuk keperluan laporan atau penyimpanan arsip.
-	Mendukung format file .xls dan .xlsx.
5.	Perhitungan Saldo Otomatis:
-	Saldo diperbarui secara otomatis setiap kali ada transaksi baru, baik pemasukan maupun pengeluaran.
6.	Validasi Input:
-	Memastikan data yang dimasukkan valid, seperti memastikan bahwa jumlah uang berupa angka dan semua kolom wajib diisi.
7.	 Kemudahan Navigasi:
-	Data yang tersimpan dapat dilihat, dipilih, dan diproses lebih lanjut (misalnya, menampilkan detail atau mengedit transaksi).
## Komponen GUI

- JFrame : Jendela utama aplikasi.
-  JPanel : Panel untuk menampung komponen lainnya.
-  JLabel : Menampilkan data transaksi dalam bentuk tabel dengan kolom seperti ID, Deskripsi, Tanggal, Jumlah, Kategori, Jenis Transaksi, dan Saldo.
-  JButton : Tombol untuk mengeksekusi perintah tertentu.
- JTextField : Input teks untuk memasukkan data tertentu
- JComboBox:Dropdown untuk memilih opsi tertentu.
-JDateChooser: untuk memilih tanggal dengan format yang seragam.
-JTable: sebagai area utama untuk menyajikan data transaksi seperti ID, Deskripsi, Tanggal, Jumlah, Kategori, Jenis Transaksi, dan Saldo.
- JScrollPane digunakan untuk menampung JTable, sehingga tabel tetap dapat dilihat sepenuhnya meskipun data yang dimasukkan sangat banyak

## Logika Program
- Program menginput deskripsi, tanggal, jumlah, kategori dan jenis transaksi untuk di input ke dalam table
- program ini juga memakai tambahan library MYSQLite 
- Program memeriksa apakah input kosong. Jika kosong, program menampilkan pesan error dengan JOptionPane
- Jika input tidak berupa angka (misalnya, huruf atau karakter spesial), program menangkap error dan juga menampilkan pesan error.
- Setelah input valid, program akan menampilkan pada Jtable apakah yang kita input.
- Jika yang kita input ada yang salah atau salah maka kalian bisa mengedit bagian yang salah dan update hasil edit
- lalu program juga bisa menghapus inputan.
- program juga bisa menyimpan hasil table menjadi .xls
- Program memeriksa apakah inputan di isi dengan benar.
- Program menampilkan hasil akhir saldo

## Instalasi

1. *Clone Repository*
   bash
   git clone https://github.com/ilfizahrah/AplikasiKeuanganPribadi.git

## Cara Penggunaan

1. *Menjalankan Aplikasi*:
   - Buka project di NetBeans.
   - Jalankan file AplikasiKeuanganPribadiFrame.java.

2. *Mengecek  saldo*:
   - Klik tombol  lihat tabungan hasil untuk melihat saldo akhir dari tabungan

## Tampilan Aplikasi Penghitung Umur (Saat Dijalankan)
![Screenshot 2024-11-22 155741](https://github.com/user-attachments/assets/cc132bd0-14d3-434e-afec-2f8ae89d47b3)


---
## Indikator Penilaian

| No  | Komponen           | Persentase |
|-----|---------------------|------------|
| 1   | Fungsional aplikasi       | 20%        |
| 2   | Desain dan UX    | 20%        |
| 3   | Penerapan konsep OOP         | 15%        |
| 4   | Kreativitas dan Inovasi fitur      | 15%        |
| 5   | Dokumentasi Kode  | 10%        |
| 6  | Tantangan | 20%        |

| *TOTAL* |               | *100%*   |

--- 
## Pembuat

Nama: Ilfi Zahrah

NPM: 2210010537

Kelas: 5B Reguler Pagi

Tugas : UTS Aplikasi Keuangan Pribadi

Fakultas : Fakultas Teknologi Informasi (FTI)

Unversitas : Universitas Islam Kalimantan Muhammad Arsyad Al Banjari Banjarmasin


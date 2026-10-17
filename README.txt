CAFÉ DIALOG + OLLAMA (AI LOKAL)

Isi folder:
- chatbotcafedialog.java : aplikasi Café Dialog
- OllamaClient.java      : penghubung ke Ollama lokal

Cara menjalankan:
1. Pastikan Ollama terpasang dan model llama3.2:1b sudah diunduh.
2. Pastikan layanan Ollama berjalan. Tes di terminal:
   ollama list
3. Buka folder ini di VS Code.
4. Compile kedua file Java bersama-sama:
   javac chatbotcafedialog.java OllamaClient.java
5. Jalankan aplikasi:
   java chatbotcafedialog
   (atau gunakan tombol Run Java di VS Code setelah kedua file ada dalam folder yang sama).

Pertanyaan yang cocok dengan jawaban bawaan Café Dialog tetap memakai data bawaan.
Pertanyaan umum yang tidak dikenali diteruskan ke model lokal llama3.2:1b.
Jawaban AI dibersihkan dari sebagian format Markdown agar lebih rapi di bubble chat.
Untuk informasi khusus kafe yang tidak tersedia, AI diarahkan agar tidak mengarang.

Catatan: gunakan Java 11 atau lebih baru karena konektor memakai java.net.http.HttpClient.
Ollama harus tetap berjalan saat fitur AI digunakan. Model bekerja secara lokal; tidak membutuhkan API key berbayar.

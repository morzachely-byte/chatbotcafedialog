
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OllamaClient {

    private static final String URL =
            "http://localhost:11434/api/chat";

    private static final String MODEL = "llama3.2:1b";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String chat(String pertanyaan) {
        try {
            String instruksi =
                    "Kamu adalah asisten virtual Café Dialog di Pringsewu, Lampung. "
                    + "Jawab dalam bahasa Indonesia yang ramah, alami, dan mudah dipahami. "
                    + "Gunakan 2 sampai 5 kalimat untuk pertanyaan umum, kecuali jika "
                    + "pengguna meminta penjelasan lebih lengkap. "
                    + "Jawab langsung ke inti dan jangan mengulang pertanyaan. "
                    + "Gunakan teks biasa tanpa Markdown, tanpa tanda bintang, "
                    + "tanpa heading, dan tanpa tabel. "
                    + "Jika membuat daftar, gunakan nomor sederhana pada baris baru. "
                    + "Utamakan informasi yang benar dan masuk akal. "
                    + "Jangan mengarang fakta, angka, kandungan gizi, harga, menu, "
                    + "jam buka, alamat, fasilitas, atau kebijakan Café Dialog. "
                    + "Jika tidak yakin, katakan bahwa informasi tersebut belum pasti. "
                    + "Untuk informasi khusus Café Dialog yang tidak tersedia, "
                    + "sarankan pengguna menghubungi admin. "
                    + "Jangan membuat klaim ilmiah tanpa dasar. "
                    + "Jangan menyatakan bahwa satu jenis kopi selalu lebih enak "
                    + "daripada jenis lainnya karena selera setiap orang berbeda.";

            String body =
                    "{\"model\":\"" + MODEL + "\","
                    + "\"stream\":false,"
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":\""
                    + escapeJson(instruksi) + "\"},"
                    + "{\"role\":\"user\",\"content\":\""
                    + escapeJson(pertanyaan) + "\"}"
                    + "]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type",
                            "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                return "Ollama mengalami error HTTP "
                        + response.statusCode()
                        + ". Pastikan Ollama berjalan.";
            }

            String result = extractContent(response.body());

            if (result == null || result.trim().isEmpty()) {
                return "Maaf, AI belum memberikan jawaban. Silakan coba lagi.";
            }

            return rapikanJawaban(result);

        } catch (java.net.ConnectException ex) {
            return "Ollama belum berjalan. Jalankan Ollama terlebih dahulu.";

        } catch (java.net.http.HttpTimeoutException ex) {
            return "AI membutuhkan waktu terlalu lama. Silakan coba lagi.";

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "Permintaan AI dibatalkan. Silakan coba lagi.";

        } catch (Exception ex) {
            return "AI lokal belum bisa dihubungi. Pastikan Ollama berjalan.";
        }
    }

    // Membersihkan format jawaban agar cocok dengan tampilan Swing.
    private static String rapikanJawaban(String teks) {
        String hasil = teks.trim()
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        // Menghapus format tebal dan miring Markdown.
        hasil = hasil.replaceAll("\\*{1,3}(.+?)\\*{1,3}", "$1");
        hasil = hasil.replaceAll("(?<!\\w)_([^_\\n]+)_(?!\\w)", "$1");

        // Menghapus heading Markdown.
        hasil = hasil.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "");

        // Mengubah bullet Markdown menjadi bullet teks biasa.
        hasil = hasil.replaceAll("(?m)^\\s*[-*+]\\s+", "• ");

        // Menghapus tanda backtick.
        hasil = hasil.replace("`", "");

        // Merapikan spasi dan baris kosong.
        hasil = hasil.replaceAll("[ \\t]+\\n", "\n");
        hasil = hasil.replaceAll("\\n{3,}", "\n\n");

        return hasil.trim();
    }

    private static String escapeJson(String teks) {
        StringBuilder hasil = new StringBuilder();

        for (int i = 0; i < teks.length(); i++) {
            char c = teks.charAt(i);

            switch (c) {
                case '"':
                    hasil.append("\\\"");
                    break;
                case '\\':
                    hasil.append("\\\\");
                    break;
                case '\b':
                    hasil.append("\\b");
                    break;
                case '\f':
                    hasil.append("\\f");
                    break;
                case '\n':
                    hasil.append("\\n");
                    break;
                case '\r':
                    hasil.append("\\r");
                    break;
                case '\t':
                    hasil.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        hasil.append(String.format("\\u%04x", (int) c));
                    } else {
                        hasil.append(c);
                    }
            }
        }

        return hasil.toString();
    }

    private static String extractContent(String json) {
        Pattern pola = Pattern.compile(
                "\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pola.matcher(json);
        String content = null;

        while (matcher.find()) {
            content = unescapeJson(matcher.group(1));
        }

        return content;
    }

    private static String unescapeJson(String teks) {
        StringBuilder hasil = new StringBuilder();

        for (int i = 0; i < teks.length(); i++) {
            char c = teks.charAt(i);

            if (c != '\\' || i + 1 >= teks.length()) {
                hasil.append(c);
                continue;
            }

            char berikutnya = teks.charAt(++i);

            switch (berikutnya) {
                case '"':
                    hasil.append('"');
                    break;
                case '\\':
                    hasil.append('\\');
                    break;
                case '/':
                    hasil.append('/');
                    break;
                case 'b':
                    hasil.append('\b');
                    break;
                case 'f':
                    hasil.append('\f');
                    break;
                case 'n':
                    hasil.append('\n');
                    break;
                case 'r':
                    hasil.append('\r');
                    break;
                case 't':
                    hasil.append('\t');
                    break;
                case 'u':
                    if (i + 4 < teks.length()) {
                        try {
                            String kode = teks.substring(i + 1, i + 5);
                            hasil.append((char) Integer.parseInt(kode, 16));
                            i += 4;
                        } catch (NumberFormatException ex) {
                            hasil.append("\\u");
                        }
                    } else {
                        hasil.append("\\u");
                    }
                    break;
                default:
                    hasil.append(berikutnya);
            }
        }

        return hasil.toString();
    }
}


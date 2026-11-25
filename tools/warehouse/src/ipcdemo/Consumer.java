package ipcdemo;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Принимающая сторона обмена списком ингредиентов склада.
 * Слушает порт, принимает одно соединение и читает кадры до конца потока.
 * Чтение накапливает данные до полного кадра, поэтому одно чтение не считается
 * одним сообщением. Конец потока между кадрами - штатное завершение, обрыв
 * внутри кадра - ошибка.
 */
public final class Consumer {

    static final int MAX_FRAME = 256;

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        String mode = args.length > 1 ? args[1] : "read";

        long pid = ProcessHandle.current().pid();
        System.out.printf("ПРИНИМАЮЩАЯ СТОРОНА: pid=%d, порт=%d, режим=%s%n", pid, port, mode);

        try (ServerSocket server = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))) {
            System.out.println("ожидание соединения");
            try (Socket socket = server.accept()) {
                System.out.println("соединение принято");
                InputStream in = socket.getInputStream();
                int count = 0;
                while (true) {
                    byte[] header = readExactly(in, 4);
                    if (header == null) {
                        System.out.printf("конец потока между кадрами после %d кадров%n", count);
                        break;
                    }
                    int length = ((header[0] & 0xFF) << 24) | ((header[1] & 0xFF) << 16)
                            | ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
                    if (length <= 0 || length > MAX_FRAME) {
                        System.out.printf("ОШИБКА: недопустимая длина кадра %d, кадр отвергнут%n", length);
                        System.exit(3);
                    }
                    byte[] payload = readExactly(in, length);
                    if (payload == null) {
                        System.out.println("ОШИБКА: обрыв посреди кадра после заголовка");
                        System.exit(4);
                    }
                    count++;
                    System.out.printf("принят кадр %d: %s%n", count, new String(payload, StandardCharsets.UTF_8));
                    if ("closeAfterFirst".equals(mode) && count == 1) {
                        System.out.println("принимающая сторона закрывает соединение после первого кадра");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("ОШИБКА ЧТЕНИЯ: " + e.getMessage());
            System.exit(5);
        }
    }

    private static byte[] readExactly(InputStream in, int len) throws IOException {
        byte[] buffer = new byte[len];
        int filled = 0;
        while (filled < len) {
            int read = in.read(buffer, filled, len - filled);
            if (read < 0) {
                if (filled == 0) {
                    return null;
                }
                throw new IOException("обрыв посреди кадра: прочитано " + filled + " из " + len + " байт");
            }
            filled += read;
        }
        return buffer;
    }
}
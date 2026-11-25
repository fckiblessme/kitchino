package ipcdemo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Передающая сторона обмена списком ингредиентов склада.
 * Открывает сокет TCP к принимающей стороне на 127.0.0.1 и отправляет кадры.
 *
 * Контракт кадра: четыре байта длины (старший байт первым) и тело в UTF-8.
 * Предельная длина кадра - 256 байт. Тело кадра: номер;название;количество.
 */
public final class Producer {

    static final int MAX_FRAME = 256;

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        int frames = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        String mode = args.length > 2 ? args[2] : "close";

        long pid = ProcessHandle.current().pid();
        System.out.printf("ПЕРЕДАЮЩАЯ СТОРОНА: pid=%d, порт=%d, кадров=%d, режим=%s%n",
                pid, port, frames, mode);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            OutputStream out = socket.getOutputStream();
            for (int i = 1; i <= frames; i++) {
                String body;
                if (i % 3 == 1) {
                    body = "1;Картофель;200.0";
                } else if (i % 3 == 2) {
                    body = "2;Морковь;150.0";
                } else {
                    body = "3;Лук;80.0";
                }
                byte[] payload = body.getBytes(StandardCharsets.UTF_8);
                if (payload.length > MAX_FRAME) {
                    throw new IllegalStateException("кадр превышает предел " + MAX_FRAME);
                }
                byte[] header = new byte[]{
                        (byte) (payload.length >>> 24),
                        (byte) (payload.length >>> 16),
                        (byte) (payload.length >>> 8),
                        (byte) payload.length};

                if ("split".equals(mode)) {
                    out.write(header);
                    out.flush();
                    Thread.sleep(400);
                    out.write(payload);
                    out.flush();
                    System.out.printf("кадр %d отправлен частями: длина и тело отдельно%n", i);
                } else {
                    byte[] frame = new byte[header.length + payload.length];
                    System.arraycopy(header, 0, frame, 0, header.length);
                    System.arraycopy(payload, 0, frame, header.length, payload.length);
                    out.write(frame);
                    out.flush();
                    System.out.printf("кадр %d отправлен, %d байт%n", i, payload.length);
                }
                Thread.sleep(300);
            }

            if ("hold".equals(mode)) {
                System.out.println("конец записи не закрыт, ожидание 15 с");
                Thread.sleep(15000);
            }
            System.out.println("передающая сторона закрывает соединение");
        } catch (IOException e) {
            System.err.println("ОШИБКА ЗАПИСИ: " + e.getMessage());
            System.exit(2);
        }
    }
}
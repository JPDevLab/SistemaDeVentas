package support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TestData {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HHmmssSSS");

    private TestData() {
    }

    public static String nombreProducto() {
        return "Producto E2E " + LocalDateTime.now().format(FORMATO);
    }

    public static String cliente(String base) {
        return base + " " + LocalDateTime.now().format(FORMATO);
    }
}

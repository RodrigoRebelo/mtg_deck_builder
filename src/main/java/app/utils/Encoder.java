package app.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Encoder {

    public static String encodeUrl(String string) {
        if (string == null) return "";
        return URLEncoder.encode(string, StandardCharsets.UTF_8).replace("+", "%20");
    }
}

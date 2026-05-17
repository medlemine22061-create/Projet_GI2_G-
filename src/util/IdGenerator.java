package util;

public class IdGenerator {
    public static String generateId(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}

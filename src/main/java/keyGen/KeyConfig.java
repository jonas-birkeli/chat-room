package keyGen;

/**
 * Configuration constants for the key generation.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public class KeyConfig {
  public static final int KEY_SIZE = 2048;
  public static final String KEY_ALGORITHM = "RSA";

  private KeyConfig() {} // Prevent instantiation
}

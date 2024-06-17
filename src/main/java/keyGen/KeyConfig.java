package keyGen;


/**
 * Configuration constants for the key generation.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public class KeyConfig {
  public static final int ASYMMETRIC_KEY_SIZE = 2048;
  public static final String ASYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT = "RSA";//ECB/OAEPWithSHA-256AndMGF1Padding";
  //public static final String ASYMMETRIC_ALGORITHM_CREATE_KEY = "RSA";
  public static final String ASYMMETRIC_ALGORITHM_CREATE_KEY = ASYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT;

  public static final int SYMMETRIC_KEY_SIZE = 128;
  public static final String SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT = "AES";//ECB/NoPadding";
  //public static final String SYMMETRIC_ALGORITHM_CREATE_KEY = "AES";
  public static final String SYMMETRIC_ALGORITHM_CREATE_KEY = SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT;

  private KeyConfig() {} // Prevent instantiation
}

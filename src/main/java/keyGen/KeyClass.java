package keyGen;

import static keyGen.KeyConfig.KEY_ALGORITHM;
import static keyGen.KeyConfig.KEY_ALGORITHM_PADDED;
import static keyGen.KeyConfig.KEY_SIZE;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

/**
 * The KeyClass class is responsible for generating keys.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public class KeyClass {
  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private PublicKey otherPartyPublicKey;

  /**
   * Constructor for the KeyClass class.
   *
   * @since 1.0
   */
  public KeyClass() {
    KeyPair keyPair;
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
      keyPairGenerator.initialize(KEY_SIZE);
      keyPair = keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to generate RSA key pair");
      keyPair = null;
    }
    assert keyPair != null;
    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();
  }

  /**
   * Get the private key.
   *
   * @return The private key
   * @since 1.0
   */
  protected PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * Get the public key.
   *
   * @return The public key
   * @since 1.0
   */
  protected PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * Set the public key of the other party.
   *
   * @param otherPartyPublicKey The public key of the other party
   * @since 1.0
   */
  protected void setOtherPartyPublicKey(PublicKey otherPartyPublicKey) {
    this.otherPartyPublicKey = otherPartyPublicKey;
  }

  /**
   * Get the public key of the other party.
   *
   * @return The public key of the other party
   * @since 1.0
   */
  protected PublicKey getOtherPartyPublicKey() {
    return otherPartyPublicKey;
  }
}

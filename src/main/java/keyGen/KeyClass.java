package keyGen;

import static keyGen.KeyConfig.ASYMMETRIC_ALGORITHM_CREATE_KEY;
import static keyGen.KeyConfig.ASYMMETRIC_KEY_SIZE;
import static keyGen.KeyConfig.SYMMETRIC_ALGORITHM_CREATE_KEY;
import static keyGen.KeyConfig.SYMMETRIC_KEY_SIZE;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * The KeyClass class is responsible for generating keys.
 *
 * @version 1.1
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public abstract class KeyClass {
  private PublicKey publicKey;
  private PrivateKey privateKey;
  private PublicKey otherPartyPublicKey;
  private SecretKey secretKey;

  /**
   * Constructor for the KeyClass class.
   * Creates the RSA key pair and the AES key.
   *
   * @since 1.0
   */
  protected KeyClass() {
    generateRSAKeyPair();
    generateAESKey();
  }

  /**
   * Generate an RSA key pair.
   * The key pair is stored in the private and public fields.
   *
   * @since 1.0
   */
  private void generateRSAKeyPair() {
    KeyPair keyPair;
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM_CREATE_KEY);
      keyPairGenerator.initialize(ASYMMETRIC_KEY_SIZE);
      keyPair = keyPairGenerator.generateKeyPair();
      privateKey = keyPair.getPrivate();
      publicKey = keyPair.getPublic();
    } catch (NoSuchAlgorithmException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to generate RSA key pair. " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Generate an AES key.
   * The key is stored in the secretKey field.
   *
   * @since 1.1
   */
  private void generateAESKey() {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM_CREATE_KEY);
      keyGenerator.init(SYMMETRIC_KEY_SIZE);
      setSecretKey(keyGenerator.generateKey());

    } catch (NoSuchAlgorithmException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to generate AES key." + e.getMessage());
    }
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
    if (otherPartyPublicKey == null) {
      throw new IllegalArgumentException("Public key cannot be null");
    }
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

  /**
   * Sets the symmetric key.
   *
   * @since 1.1
   */
  protected void setSecretKey(SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  /**
   * Gets the symmetric key.
   *
   * @return The symmetric key
   * @since 1.1
   */
  protected SecretKey getSecretKey() {
    return secretKey;
  }
}

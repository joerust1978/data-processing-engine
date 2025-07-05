package com.fifththird.edo.processingcore.util;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.pgpainless.PGPainless;
import org.pgpainless.decryption_verification.ConsumerOptions;
import org.pgpainless.decryption_verification.DecryptionStream;
import org.pgpainless.encryption_signing.EncryptionOptions;
import org.pgpainless.encryption_signing.EncryptionStream;
import org.pgpainless.encryption_signing.ProducerOptions;
import org.pgpainless.key.info.KeyRingInfo;
import org.pgpainless.key.protection.SecretKeyRingProtector;
import org.pgpainless.util.Passphrase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for PGP operations using PGP Painless.
 * Provides methods to wrap InputStreams with PGP decryption capabilities.
 */
@Component
public class PgpUtilities {

    private static final Logger logger = LoggerFactory.getLogger(PgpUtilities.class);

    /**
     * Wraps an InputStream with PGP decryption using a private key.
     * 
     * @param encryptedInputStream the encrypted input stream
     * @param privateKeyString the PGP private key as a string
     * @param passphrase the passphrase for the private key
     * @return decrypted InputStream
     * @throws PGPException if PGP operations fail
     * @throws IOException if I/O operations fail
     */
    public InputStream wrapWithDecryption(InputStream encryptedInputStream, 
                                        String privateKeyString, 
                                        String passphrase) 
            throws PGPException, IOException {
        
        logger.debug("Wrapping InputStream with PGP decryption using private key string");
        
        try {
            // Convert private key string to PGPSecretKeyRing
            PGPSecretKeyRing privateKeyRing = loadSecretKeyRingFromString(privateKeyString);
            
            // Create protector for the private key
            SecretKeyRingProtector protector = SecretKeyRingProtector.unlockAnyKeyWith(Passphrase.fromPassword(passphrase));
            
            // Configure decryption options
            ConsumerOptions options = new ConsumerOptions()
                    .addDecryptionKey(privateKeyRing, protector);
            
            // Create decryption stream
            DecryptionStream decryptionStream = PGPainless.decryptAndOrVerify()
                    .onInputStream(encryptedInputStream)
                    .withOptions(options);
            
            logger.debug("PGP decryption stream created successfully");
            return decryptionStream;
            
        } catch (Exception e) {
            logger.error("Failed to create PGP decryption stream", e);
            throw new PGPException("Failed to initialize PGP decryption", e);
        }
    }

    /**
     * Loads a PGP secret key ring from a string.
     * 
     * @param privateKeyString the PGP private key as a string
     * @return PGP secret key ring
     * @throws IOException if I/O operations fail
     * @throws PGPException if PGP operations fail
     */
    public PGPSecretKeyRing loadSecretKeyRingFromString(String privateKeyString) 
            throws IOException, PGPException {
        
        logger.debug("Loading PGP secret key ring from string");
        
        try {
            InputStream keyInputStream = new ByteArrayInputStream(privateKeyString.getBytes());
            PGPSecretKeyRing secretKeyRing = PGPainless.readKeyRing()
                    .secretKeyRing(keyInputStream);
            
            logger.debug("PGP secret key ring loaded successfully from string");
            return secretKeyRing;
            
        } catch (IOException e) {
            logger.error("Failed to load PGP secret key ring from string", e);
            throw e;
        }
    }

    /**
     * Loads a PGP secret key ring from an InputStream.
     * 
     * @param keyInputStream the input stream containing the PGP secret key
     * @return PGP secret key ring
     * @throws IOException if I/O operations fail
     * @throws PGPException if PGP operations fail
     */
    public PGPSecretKeyRing loadSecretKeyRing(InputStream keyInputStream) 
            throws IOException, PGPException {
        
        logger.debug("Loading PGP secret key ring from InputStream");
        
        try {
            PGPSecretKeyRing secretKeyRing = PGPainless.readKeyRing()
                    .secretKeyRing(keyInputStream);
            
            logger.debug("PGP secret key ring loaded successfully");
            return secretKeyRing;
            
        } catch (IOException e) {
            logger.error("Failed to load PGP secret key ring", e);
            throw e;
        }
    }

    /**
     * Loads a PGP public key ring from an InputStream.
     * 
     * @param keyInputStream the input stream containing the PGP public key
     * @return PGP public key ring
     * @throws IOException if I/O operations fail
     * @throws PGPException if PGP operations fail
     */
    public PGPPublicKeyRing loadPublicKeyRing(InputStream keyInputStream) 
            throws IOException, PGPException {
        
        logger.debug("Loading PGP public key ring from InputStream");
        
        try {
            PGPPublicKeyRing publicKeyRing = PGPainless.readKeyRing()
                    .publicKeyRing(keyInputStream);
            
            logger.debug("PGP public key ring loaded successfully");
            return publicKeyRing;
            
        } catch (IOException e) {
            logger.error("Failed to load PGP public key ring", e);
            throw e;
        }
    }

    /**
     * Gets information about a PGP key ring.
     * 
     * @param secretKeyRing the PGP secret key ring
     * @return key ring information
     */
    public KeyRingInfo getKeyRingInfo(PGPSecretKeyRing secretKeyRing) {
        return new KeyRingInfo(secretKeyRing);
    }

   

    /**
     * Checks if a key ring contains signing keys.
     * 
     * @param secretKeyRing the PGP secret key ring
     * @return true if the key ring contains signing keys
     */
    public boolean hasSigningKeys(PGPSecretKeyRing secretKeyRing) {
        KeyRingInfo keyRingInfo = new KeyRingInfo(secretKeyRing);
        return keyRingInfo.getSigningSubkeys().size() > 0;
    }

    /**
     * Wraps an OutputStream with PGP encryption using a public key string.
     * 
     * @param plaintextOutputStream the plaintext output stream
     * @param publicKeyString the PGP public key as a string
     * @return encrypted OutputStream
     * @throws PGPException if PGP operations fail
     * @throws IOException if I/O operations fail
     */
    public OutputStream wrapWithEncryption(OutputStream plaintextOutputStream, 
                                        String publicKeyString) 
            throws PGPException, IOException {
        
        logger.debug("Wrapping OutputStream with PGP encryption using public key string");
        
        try {
            // Convert public key string to PGPPublicKeyRing
            PGPPublicKeyRing publicKeyRing = loadPublicKeyRingFromString(publicKeyString);
            
            // Configure encryption options
            ProducerOptions options = ProducerOptions.encrypt(EncryptionOptions.get().addRecipient(publicKeyRing));
            
            // Create encryption stream
            EncryptionStream encryptionStream = PGPainless.encryptAndOrSign()
                    .onOutputStream(plaintextOutputStream)
                    .withOptions(options);
            
            logger.debug("PGP encryption stream created successfully");
            return encryptionStream;
            
        } catch (Exception e) {
            logger.error("Failed to create PGP encryption stream", e);
            throw new PGPException("Failed to initialize PGP encryption", e);
        }
    }

    /**
     * Loads a PGP public key ring from a string.
     * 
     * @param publicKeyString the PGP public key as a string
     * @return PGP public key ring
     * @throws IOException if I/O operations fail
     * @throws PGPException if PGP operations fail
     */
    public PGPPublicKeyRing loadPublicKeyRingFromString(String publicKeyString) 
            throws IOException, PGPException {
        
        logger.debug("Loading PGP public key ring from string");
        
        try {
            InputStream keyInputStream = new ByteArrayInputStream(publicKeyString.getBytes());
            PGPPublicKeyRing publicKeyRing = PGPainless.readKeyRing()
                    .publicKeyRing(keyInputStream);
            
            logger.debug("PGP public key ring loaded successfully from string");
            return publicKeyRing;
            
        } catch (IOException e) {
            logger.error("Failed to load PGP public key ring from string", e);
            throw e;
        }
    }
} 
package com.alam.payment.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    /**
     * Path where the RSA key pair is persisted between restarts.
     * Override via jwt.key-store-dir property (default: current working dir).
     */
    @Value("${jwt.key-store-dir:./keys}")
    private String keyStoreDir;

    /**
     * Loads the RSA key pair from disk, generating and saving it on first run.
     * This prevents the "all JWTs invalidated on restart" bug caused by
     * generating a new key pair every time the application starts.
     */
    @Bean
    public KeyPair keyPair() throws Exception {

        Path dir = Paths.get(keyStoreDir);
        Path privateKeyPath = dir.resolve("jwt-private.pem");
        Path publicKeyPath  = dir.resolve("jwt-public.pem");

        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            return loadKeyPair(privateKeyPath, publicKeyPath);
        }

        // First run: generate, persist, then return
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        Files.createDirectories(dir);
        saveKey(privateKeyPath, keyPair.getPrivate().getEncoded());
        saveKey(publicKeyPath,  keyPair.getPublic().getEncoded());

        return keyPair;
    }

    private void saveKey(Path path, byte[] keyBytes) throws IOException {
        Files.writeString(path, Base64.getEncoder().encodeToString(keyBytes));
    }

    private KeyPair loadKeyPair(Path privateKeyPath, Path publicKeyPath) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");

        byte[] privateBytes = Base64.getDecoder().decode(Files.readString(privateKeyPath).trim());
        byte[] publicBytes  = Base64.getDecoder().decode(Files.readString(publicKeyPath).trim());

        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        RSAPublicKey  publicKey  = (RSAPublicKey)  kf.generatePublic(new X509EncodedKeySpec(publicBytes));

        return new KeyPair(publicKey, privateKey);
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {

        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {

        return NimbusJwtDecoder
                .withPublicKey((RSAPublicKey) keyPair.getPublic())
                .build();
    }
}
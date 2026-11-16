package br.edu.ufcg.psoftarena.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class CryptoConfig {

    @Bean
    @SuppressWarnings("deprecation")
    public TextEncryptor textEncryptor(@Value("${APP_CRYPTO_SECRET}") String secret) {
        return Encryptors.delux(secret, "5c0744940b5c369b");
    }
}

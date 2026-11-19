package com.psoft.arena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CriptografiaService {
    private final SecretKeySpec chave;
    private final SecureRandom random = new SecureRandom();
    public CriptografiaService(@Value("${arena.llm.chave-mestra:${ARENA_LLM_CHAVE_MESTRA:}}") String base64) {
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(base64); }
        catch (IllegalArgumentException ex) { throw new IllegalStateException("Chave mestra inválida."); }
        if (bytes.length != 32) throw new IllegalStateException(
                "Configure ARENA_LLM_CHAVE_MESTRA com 32 bytes aleatórios em Base64.");
        chave = new SecretKeySpec(bytes, "AES");
    }
    public String criptografar(String texto) {
        try {
            byte[] iv = new byte[12]; random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(128, iv));
            byte[] resultado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + resultado.length)
                    .put(iv).put(resultado).array());
        } catch (GeneralSecurityException ex) { throw new IllegalStateException("Falha ao proteger credencial."); }
    }
    public String descriptografar(String texto) {
        try {
            byte[] dados = Base64.getDecoder().decode(texto);
            if (dados.length < 28) throw new IllegalArgumentException();
            ByteBuffer buffer = ByteBuffer.wrap(dados);
            byte[] iv = new byte[12]; buffer.get(iv);
            byte[] conteudo = new byte[buffer.remaining()]; buffer.get(conteudo);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(conteudo), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Falha ao recuperar credencial.");
        }
    }
}

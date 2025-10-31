/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
/**
 *
 * @author PC
 */
public class FileLogger {
    private final Path file;

    /**
     * Crea un logger que escribe en la ruta indicada (ej: "./bitacora.txt")
     */
    public FileLogger(String path) {
        Objects.requireNonNull(path);
        this.file = Paths.get(path);
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent() == null ? Paths.get(".") : file.getParent());
                Files.createFile(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar FileLogger en " + path, e);
        }
    }

    /**
     * Registra una entrada en la bitácora.
     * @param origin  origen de la operación (ej. "SQLSERVER" o "ORACLE")
     * @param op      operación (INSERT, UPDATE, DELETE, READ, ERROR, etc.)
     * @param id      identificador (DPI) -- tipo long
     * @param payload contenido adicional (si es JSON pasa crudo, si no será escapado)
     * @param status  estado (APPLIED, FAILED, etc.)
     */
    public synchronized void log(String origin, String op, long id, String payload, String status) {
        String ts = OffsetDateTime.now().toString();
        String tx = UUID.randomUUID().toString();

        // payload: si ya parece JSON (empieza con { o [) lo dejamos tal cual; si no, lo escapeamos en comillas
        String payloadOut;
        if (payload == null || payload.isBlank()) {
            payloadOut = "{}";
        } else {
            String trimmed = payload.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                payloadOut = trimmed;
            } else {
                payloadOut = "\"" + escapeJson(trimmed) + "\"";
            }
        }

        String line = String.format("{\"ts\":\"%s\",\"tx\":\"%s\",\"origin\":\"%s\",\"op\":\"%s\",\"id\":%d,\"payload\":%s,\"status\":\"%s\"}",
                ts, tx, escapeJson(origin), escapeJson(op), id, payloadOut, escapeJson(status));

        // escribir en archivo (append) y en consola
        try {
            Files.write(file, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println(line);
        } catch (IOException e) {
            // No lanzar excepción para no romper la app; imprimir en consola
            System.err.println("FileLogger: error escribiendo log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escape simple para JSON de strings (escapa comillas y barras y control chars básicos).
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}

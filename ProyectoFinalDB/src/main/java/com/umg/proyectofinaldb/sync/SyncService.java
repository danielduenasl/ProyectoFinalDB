/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb.sync;

import com.umg.proyectofinaldb.model.Registro;
import com.umg.proyectofinaldb.db.RecordDAO;
import com.umg.proyectofinaldb.util.FileLogger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 *
 * @author PC
 */
public class SyncService {
    private final RecordDAO dao = new RecordDAO();
    private final FileLogger logger;

    public SyncService(FileLogger logger) {
        this.logger = logger;
    }

    public void sincronizar(DataSource dsA, String nameA, DataSource dsB, String nameB) throws SQLException {
        System.out.println("SYNC: Inicio sincronización entre " + nameA + " <-> " + nameB);
        List<Registro> listA;
        List<Registro> listB;

        try {
            listA = dao.getAll(dsA);
        } catch (SQLException ex) {
            System.err.println("SYNC: Error leyendo datos de " + nameA + ": " + ex.getMessage());
            ex.printStackTrace();
            logger.log(nameA, "READ", -1, ex.getMessage(), "FAILED");
            return; // no podemos continuar sin leer A
        }

        try {
            listB = dao.getAll(dsB);
        } catch (SQLException ex) {
            System.err.println("SYNC: Error leyendo datos de " + nameB + ": " + ex.getMessage());
            ex.printStackTrace();
            logger.log(nameB, "READ", -1, ex.getMessage(), "FAILED");
            return; // no podemos continuar sin leer B
        }

        System.out.println("SYNC: registros " + nameA + " = " + listA.size() + ", " + nameB + " = " + listB.size());
        System.out.println("SYNC: ids " + nameA + " => " + joinIds(listA));
        System.out.println("SYNC: ids " + nameB + " => " + joinIds(listB));

        Map<Long, Registro> mapA = new HashMap<>();
        Map<Long, Registro> mapB = new HashMap<>();
        for (Registro r : listA) mapA.put(r.getDpi(), r);
        for (Registro r : listB) mapB.put(r.getDpi(), r);

        Set<Long> allIds = new HashSet<>();
        allIds.addAll(mapA.keySet());
        allIds.addAll(mapB.keySet());

        for (Long dpi : allIds) {
            Registro ra = mapA.get(dpi);
            Registro rb = mapB.get(dpi);

            if (ra != null && rb == null) {
                // existe en A, no en B -> insertar en B
                System.out.println("SYNC: Intentando INSERT en " + nameB + " id=" + dpi);
                try {
                    dao.insert(dsB, ra);
                    logger.log(nameA, "INSERT", ra.getDpi(), "{}", "APPLIED");
                    System.out.println("SYNC: INSERT aplicado en " + nameB + " id=" + dpi);
                } catch (SQLException ex) {
                    System.err.println("SYNC: ERROR al INSERT en " + nameB + " id=" + dpi + " -> " + ex.getMessage());
                    ex.printStackTrace();
                    logger.log(nameA, "INSERT", ra.getDpi(), ex.getMessage(), "FAILED");
                }
            } else if (ra == null && rb != null) {
                // existe en B, no en A -> insertar en A
                System.out.println("SYNC: Intentando INSERT en " + nameA + " id=" + dpi);
                try {
                    dao.insert(dsA, rb);
                    logger.log(nameB, "INSERT", rb.getDpi(), "{}", "APPLIED");
                    System.out.println("SYNC: INSERT aplicado en " + nameA + " id=" + dpi);
                } catch (SQLException ex) {
                    System.err.println("SYNC: ERROR al INSERT en " + nameA + " id=" + dpi + " -> " + ex.getMessage());
                    ex.printStackTrace();
                    logger.log(nameB, "INSERT", rb.getDpi(), ex.getMessage(), "FAILED");
                }
            } else if (ra != null && rb != null) {
                // ambos existen: comparar lastModified
                OffsetDateTime aTs = ra.getLastModified();
                OffsetDateTime bTs = rb.getLastModified();

                try {
                    if (aTs != null && bTs != null) {
                        if (aTs.isAfter(bTs)) {
                            System.out.println("SYNC: UPDATE en " + nameB + " id=" + dpi + " (A más reciente)");
                            dao.update(dsB, ra);
                            logger.log(nameA, "UPDATE", ra.getDpi(), "{}", "APPLIED");
                        } else if (bTs.isAfter(aTs)) {
                            System.out.println("SYNC: UPDATE en " + nameA + " id=" + dpi + " (B más reciente)");
                            dao.update(dsA, rb);
                            logger.log(nameB, "UPDATE", rb.getDpi(), "{}", "APPLIED");
                        } else {
                            // timestamps iguales -> no hacer nada
                            // opcional: log de igualdad
                        }
                    } else {
                        // si falta timestamp, aplicar la versión A por defecto (puedes cambiar la política)
                        System.out.println("SYNC: Timestamps faltantes; aplicando UPDATE en " + nameB + " id=" + dpi);
                        dao.update(dsB, ra);
                        logger.log(nameA, "UPDATE", ra.getDpi(), "{}", "APPLIED");
                    }
                } catch (SQLException ex) {
                    System.err.println("SYNC: ERROR al UPDATE para id=" + dpi + " -> " + ex.getMessage());
                    ex.printStackTrace();
                    logger.log("SYNC", "ERROR", dpi, ex.getMessage(), "FAILED");
                }
            }
        }

        System.out.println("SYNC: Sincronización finalizada entre " + nameA + " <-> " + nameB);
    }

    private String joinIds(List<Registro> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().map(r -> String.valueOf(r.getDpi())).collect(Collectors.joining(","));
    }
}

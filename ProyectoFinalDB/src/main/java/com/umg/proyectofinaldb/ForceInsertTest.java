/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import com.umg.proyectofinaldb.db.RecordDAO;
import com.umg.proyectofinaldb.db.DBConnectionManager;
import com.umg.proyectofinaldb.model.Registro;
/**
 *
 * @author PC
 */
public class ForceInsertTest {
    public static void main(String[] args) {
        try {
            DBConnectionManager dbm = new DBConnectionManager();
            dbm.loadFromProperties(); // imprime debug de URLs

            DataSource dsSql = dbm.createSqlServerDataSource();
            DataSource dsOracle = dbm.createOracleDataSource();

            System.out.println("DEBUG: dsSql = " + dsSql);
            System.out.println("DEBUG: dsOracle = " + dsOracle);

            RecordDAO dao = new RecordDAO();
            List<Registro> lista = dao.getAll(dsOracle);
            System.out.println("ORACLE rows = " + lista.size());
            for (Registro r : lista) {
                try {
                    System.out.println("FORCE insert DPI=" + r.getDpi() + " nombre=" + r.getPrimerNombre());
                    dao.insert(dsSql, r);
                    System.out.println("OK DPI=" + r.getDpi());
                } catch (SQLException ex) {
                    System.err.println("FAIL DPI=" + r.getDpi() + " -> " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.util.Properties;
import java.io.InputStream;
/**
 *
 * @author PC
 */
public class DBConnectionManager {
    private HikariDataSource sqlServerDs;
    private HikariDataSource oracleDs;
    private Properties props; // guardaremos los props cargados

    // Carga app.properties y muestra debug
    public void loadFromProperties() throws Exception {
        props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("app.properties")) {

            if (in == null) {
                System.out.println("DEBUG: app.properties NO encontrado en classpath");
                return;
            }

            props.load(in);

            // --- DEBUG: imprime lo que está leyendo ---
            System.out.println("DEBUG: sqlserver.jdbcUrl = " + props.getProperty("sqlserver.jdbcUrl"));
            System.out.println("DEBUG: sqlserver.user = " + props.getProperty("sqlserver.user"));
            System.out.println("DEBUG: sqlserver.password = " + props.getProperty("sqlserver.password"));
            System.out.println("DEBUG: oracle.jdbcUrl = " + props.getProperty("oracle.jdbcUrl"));
            System.out.println("DEBUG: oracle.user = " + props.getProperty("oracle.user"));
            System.out.println("DEBUG: oracle.password = " + props.getProperty("oracle.password"));
            // -----------------------------------------
        }
    }

    // Crear DataSource de SQL Server usando propiedades cargadas
    public DataSource createSqlServerDataSource() throws Exception {
        if (sqlServerDs != null) sqlServerDs.close();
        if (props == null) loadFromProperties(); // asegúrate de tener las props

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getProperty("sqlserver.jdbcUrl"));
        cfg.setUsername(props.getProperty("sqlserver.user"));
        cfg.setPassword(props.getProperty("sqlserver.password"));
        cfg.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        cfg.setMaximumPoolSize(10);
        sqlServerDs = new HikariDataSource(cfg);
        return sqlServerDs;
    }

    // Crear DataSource de Oracle usando propiedades cargadas
    public DataSource createOracleDataSource() throws Exception {
        if (oracleDs != null) oracleDs.close();
        if (props == null) loadFromProperties(); // asegúrate de tener las props

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getProperty("oracle.jdbcUrl"));
        cfg.setUsername(props.getProperty("oracle.user"));
        cfg.setPassword(props.getProperty("oracle.password"));
        cfg.setDriverClassName("oracle.jdbc.OracleDriver");
        cfg.setMaximumPoolSize(10);
        oracleDs = new HikariDataSource(cfg);
        return oracleDs;
    }

    public void closeAll() {
        if (sqlServerDs != null) sqlServerDs.close();
        if (oracleDs != null) oracleDs.close();
    }

    public DataSource getSqlServerDs() { return sqlServerDs; }
    public DataSource getOracleDs() { return oracleDs; }
}

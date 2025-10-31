/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb.db;

import com.umg.proyectofinaldb.model.Registro;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author PC
 */
public class RecordDAO {
    public List<Registro> getAll(DataSource ds) throws SQLException {
        String sql = "SELECT DPI, PRIMER_NOMBRE, SEGUNDO_NOMBRE, PRIMER_APELLIDO, SEGUNDO_APELLIDO, DIRECCION_DOMICILIAR, TELEFONO_DE_CASA, TELEFONO_MOVIL, SALARIO_BASE, BONIFICACION, LAST_MODIFIED FROM REGISTRO";
        List<Registro> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Registro r = new Registro();
                r.setDpi(rs.getLong("DPI"));
                r.setPrimerNombre(rs.getString("PRIMER_NOMBRE"));
                r.setSegundoNombre(rs.getString("SEGUNDO_NOMBRE"));
                r.setPrimerApellido(rs.getString("PRIMER_APELLIDO"));
                r.setSegundoApellido(rs.getString("SEGUNDO_APELLIDO"));
                r.setDireccionDomiciliar(rs.getString("DIRECCION_DOMICILIAR"));
                r.setTelefonoCasa(rs.getString("TELEFONO_DE_CASA"));
                r.setTelefonoMovil(rs.getString("TELEFONO_MOVIL"));
                BigDecimal sal = rs.getBigDecimal("SALARIO_BASE");
                r.setSalarioBase(sal);
                BigDecimal bon = rs.getBigDecimal("BONIFICACION");
                r.setBonificacion(bon);
                Timestamp ts = rs.getTimestamp("LAST_MODIFIED");
                if (ts != null) r.setLastModified(ts.toInstant().atOffset(ZoneOffset.UTC));
                list.add(r);
            }
        }
        return list;
    }

    public void insert(DataSource ds, Registro r) throws SQLException {
        String sql = "INSERT INTO REGISTRO (DPI, PRIMER_NOMBRE, SEGUNDO_NOMBRE, PRIMER_APELLIDO, SEGUNDO_APELLIDO, DIRECCION_DOMICILIAR, TELEFONO_DE_CASA, TELEFONO_MOVIL, SALARIO_BASE, BONIFICACION, LAST_MODIFIED) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, r.getDpi());
            ps.setString(2, r.getPrimerNombre());
            ps.setString(3, r.getSegundoNombre());
            ps.setString(4, r.getPrimerApellido());
            ps.setString(5, r.getSegundoApellido());
            ps.setString(6, r.getDireccionDomiciliar());
            ps.setString(7, r.getTelefonoCasa());
            ps.setString(8, r.getTelefonoMovil());
            if (r.getSalarioBase() != null) ps.setBigDecimal(9, r.getSalarioBase()); else ps.setNull(9, Types.DECIMAL);
            if (r.getBonificacion() != null) ps.setBigDecimal(10, r.getBonificacion()); else ps.setNull(10, Types.DECIMAL);
            Timestamp ts = r.getLastModified() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.from(r.getLastModified().toInstant());
            ps.setTimestamp(11, ts);
            ps.executeUpdate();
        }
    }

    public void update(DataSource ds, Registro r) throws SQLException {
        String sql = "UPDATE REGISTRO SET PRIMER_NOMBRE=?, SEGUNDO_NOMBRE=?, PRIMER_APELLIDO=?, SEGUNDO_APELLIDO=?, DIRECCION_DOMICILIAR=?, TELEFONO_DE_CASA=?, TELEFONO_MOVIL=?, SALARIO_BASE=?, BONIFICACION=?, LAST_MODIFIED=? WHERE DPI=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getPrimerNombre());
            ps.setString(2, r.getSegundoNombre());
            ps.setString(3, r.getPrimerApellido());
            ps.setString(4, r.getSegundoApellido());
            ps.setString(5, r.getDireccionDomiciliar());
            ps.setString(6, r.getTelefonoCasa());
            ps.setString(7, r.getTelefonoMovil());
            if (r.getSalarioBase() != null) ps.setBigDecimal(8, r.getSalarioBase()); else ps.setNull(8, Types.DECIMAL);
            if (r.getBonificacion() != null) ps.setBigDecimal(9, r.getBonificacion()); else ps.setNull(9, Types.DECIMAL);
            Timestamp ts = r.getLastModified() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.from(r.getLastModified().toInstant());
            ps.setTimestamp(10, ts);
            ps.setLong(11, r.getDpi());
            ps.executeUpdate();
        }
    }

    public void delete(DataSource ds, long dpi) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM REGISTRO WHERE DPI=?")) {
            ps.setLong(1, dpi);
            ps.executeUpdate();
        }
    }
    
    public Registro getByDpi(DataSource ds, long dpi) throws SQLException {
    String sql = "SELECT DPI, PRIMER_NOMBRE, SEGUNDO_NOMBRE, PRIMER_APELLIDO, SEGUNDO_APELLIDO, DIRECCION_DOMICILIAR, TELEFONO_DE_CASA, TELEFONO_MOVIL, SALARIO_BASE, BONIFICACION, LAST_MODIFIED FROM REGISTRO WHERE DPI = ?";
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, dpi);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Registro r = new Registro();
                r.setDpi(rs.getLong("DPI"));
                r.setPrimerNombre(rs.getString("PRIMER_NOMBRE"));
                r.setSegundoNombre(rs.getString("SEGUNDO_NOMBRE"));
                r.setPrimerApellido(rs.getString("PRIMER_APELLIDO"));
                r.setSegundoApellido(rs.getString("SEGUNDO_APELLIDO"));
                r.setDireccionDomiciliar(rs.getString("DIRECCION_DOMICILIAR"));
                r.setTelefonoCasa(rs.getString("TELEFONO_DE_CASA"));
                r.setTelefonoMovil(rs.getString("TELEFONO_MOVIL"));
                r.setSalarioBase(rs.getBigDecimal("SALARIO_BASE"));
                r.setBonificacion(rs.getBigDecimal("BONIFICACION"));
                Timestamp ts = rs.getTimestamp("LAST_MODIFIED");
                if (ts != null) r.setLastModified(ts.toInstant().atOffset(ZoneOffset.UTC));
                return r;
            }
            return null;
        }
    }
}
}

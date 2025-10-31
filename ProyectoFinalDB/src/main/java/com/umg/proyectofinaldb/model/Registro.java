/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
/**
 *
 * @author PC
 */
public class Registro {
    private long dpi;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String direccionDomiciliar;
    private String telefonoCasa;
    private String telefonoMovil;
    private BigDecimal salarioBase;
    private BigDecimal bonificacion;
    private OffsetDateTime lastModified;

    // getters y setters
    public long getDpi() { return dpi; }
    public void setDpi(long dpi) { this.dpi = dpi; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getDireccionDomiciliar() { return direccionDomiciliar; }
    public void setDireccionDomiciliar(String direccionDomiciliar) { this.direccionDomiciliar = direccionDomiciliar; }

    public String getTelefonoCasa() { return telefonoCasa; }
    public void setTelefonoCasa(String telefonoCasa) { this.telefonoCasa = telefonoCasa; }

    public String getTelefonoMovil() { return telefonoMovil; }
    public void setTelefonoMovil(String telefonoMovil) { this.telefonoMovil = telefonoMovil; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public BigDecimal getBonificacion() { return bonificacion; }
    public void setBonificacion(BigDecimal bonificacion) { this.bonificacion = bonificacion; }

    public OffsetDateTime getLastModified() { return lastModified; }
    public void setLastModified(OffsetDateTime lastModified) { this.lastModified = lastModified; }
    
}

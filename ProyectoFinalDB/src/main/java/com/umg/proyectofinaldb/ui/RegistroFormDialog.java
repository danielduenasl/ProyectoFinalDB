/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.proyectofinaldb.ui;

import com.umg.proyectofinaldb.model.Registro;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
/**
 *
 * @author PC
 */
public class RegistroFormDialog extends JDialog {
    private JTextField txtDpi, txtPrimerNombre, txtSegundoNombre, txtPrimerApellido, txtSegundoApellido,
            txtDireccion, txtTelCasa, txtTelMovil, txtSalario, txtBonificacion;

    private Registro resultado; // aquí guardamos el resultado final (nuevo o editado)

    public RegistroFormDialog(Window parent, Registro existente) {
        super(parent, "Registro", ModalityType.APPLICATION_MODAL);

        // Panel con grid para campos
        JPanel form = new JPanel(new GridLayout(11, 2, 6, 6));
        form.add(new JLabel("DPI:"));
        txtDpi = new JTextField();
        form.add(txtDpi);

        form.add(new JLabel("Primer Nombre:"));
        txtPrimerNombre = new JTextField();
        form.add(txtPrimerNombre);

        form.add(new JLabel("Segundo Nombre:"));
        txtSegundoNombre = new JTextField();
        form.add(txtSegundoNombre);

        form.add(new JLabel("Primer Apellido:"));
        txtPrimerApellido = new JTextField();
        form.add(txtPrimerApellido);

        form.add(new JLabel("Segundo Apellido:"));
        txtSegundoApellido = new JTextField();
        form.add(txtSegundoApellido);

        form.add(new JLabel("Dirección:"));
        txtDireccion = new JTextField();
        form.add(txtDireccion);

        form.add(new JLabel("Tel. Casa:"));
        txtTelCasa = new JTextField();
        form.add(txtTelCasa);

        form.add(new JLabel("Tel. Móvil:"));
        txtTelMovil = new JTextField();
        form.add(txtTelMovil);

        form.add(new JLabel("Salario Base:"));
        txtSalario = new JTextField();
        form.add(txtSalario);

        form.add(new JLabel("Bonificación:"));
        txtBonificacion = new JTextField();
        form.add(txtBonificacion);

        // Botonera
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        actions.add(btnCancelar);
        actions.add(btnGuardar);

        // Contenedor principal
        setLayout(new BorderLayout(8, 8));
        add(new JScrollPane(form), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        // Cargar datos si es edición
        if (existente != null) {
            txtDpi.setText(String.valueOf(existente.getDpi()));
            txtDpi.setEnabled(false); // DPI no se edita
            txtPrimerNombre.setText(nvl(existente.getPrimerNombre()));
            txtSegundoNombre.setText(nvl(existente.getSegundoNombre()));
            txtPrimerApellido.setText(nvl(existente.getPrimerApellido()));
            txtSegundoApellido.setText(nvl(existente.getSegundoApellido()));
            txtDireccion.setText(nvl(existente.getDireccionDomiciliar()));
            txtTelCasa.setText(nvl(existente.getTelefonoCasa())); // <-- ajusta si tu getter se llama distinto
            txtTelMovil.setText(nvl(existente.getTelefonoMovil()));
            txtSalario.setText(existente.getSalarioBase() == null ? "" : existente.getSalarioBase().toPlainString());
            txtBonificacion.setText(existente.getBonificacion() == null ? "" : existente.getBonificacion().toPlainString());
        }

        // Acciones
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> {
            resultado = null;
            setVisible(false);
        });

        setSize(520, 460);
        setLocationRelativeTo(parent);
    }

    private void onGuardar() {
        try {
            // Validaciones mínimas
            String dpiTxt = req(txtDpi.getText(), "DPI");
            if (!dpiTxt.matches("\\d{7,18}")) {
                throw new IllegalArgumentException("DPI debe ser numérico (7-18 dígitos).");
            }
            long dpi = Long.parseLong(dpiTxt);

            String pNom = req(txtPrimerNombre.getText(), "Primer Nombre");
            String pApe = req(txtPrimerApellido.getText(), "Primer Apellido");

            String sNom = opt(txtSegundoNombre.getText());
            String sApe = opt(txtSegundoApellido.getText());
            String dir  = opt(txtDireccion.getText());
            String tCasa  = opt(txtTelCasa.getText());
            String tMovil = opt(txtTelMovil.getText());

            BigDecimal salario = parseBigDec(txtSalario.getText(), "Salario Base", true);
            BigDecimal bonif   = parseBigDec(txtBonificacion.getText(), "Bonificación", true);

            Registro r = new Registro();
            r.setDpi(dpi);
            r.setPrimerNombre(pNom);
            r.setSegundoNombre(sNom);
            r.setPrimerApellido(pApe);
            r.setSegundoApellido(sApe);
            r.setDireccionDomiciliar(dir);
            r.setTelefonoCasa(tCasa);     // <-- ajusta si tu setter se llama distinto
            r.setTelefonoMovil(tMovil);
            r.setSalarioBase(salario);
            r.setBonificacion(bonif);

            this.resultado = r;
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Registro getRegistro() {
        return resultado;
    }

    // ---------------- utils ----------------
    private static String nvl(String s) { return (s == null) ? "" : s; }
    private static String req(String s, String campo) {
        String v = s == null ? "" : s.trim();
        if (v.isEmpty()) throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        return v;
    }
    private static String opt(String s) { return s == null ? "" : s.trim(); }

    private static BigDecimal parseBigDec(String text, String campo, boolean allowEmpty) {
        String v = text == null ? "" : text.trim();
        if (v.isEmpty()) return allowEmpty ? null : BigDecimal.ZERO;
        try {
            // evita problemas de coma vs punto
            v = v.replace(',', '.');
            return new BigDecimal(v);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser numérico (ej: 1234.56).");
        }
    }
}

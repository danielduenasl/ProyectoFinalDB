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
    private JTextField txtDpi, txtPrimerNombre, txtPrimerApellido, txtSalario;
    private JButton btnOk, btnCancel;
    private Registro registro;

    public RegistroFormDialog(Window owner, Registro existing) {
        super(owner, "Registro", ModalityType.APPLICATION_MODAL);
        setSize(400, 250);
        setLocationRelativeTo(owner);
        setLayout(null);

        JLabel l1 = new JLabel("DPI:");
        l1.setBounds(20, 20, 120, 25);
        add(l1);
        txtDpi = new JTextField();
        txtDpi.setBounds(150, 20, 200, 25);
        add(txtDpi);

        JLabel l2 = new JLabel("Primer Nombre:");
        l2.setBounds(20, 60, 120, 25);
        add(l2);
        txtPrimerNombre = new JTextField();
        txtPrimerNombre.setBounds(150, 60, 200, 25);
        add(txtPrimerNombre);

        JLabel l3 = new JLabel("Primer Apellido:");
        l3.setBounds(20, 100, 120, 25);
        add(l3);
        txtPrimerApellido = new JTextField();
        txtPrimerApellido.setBounds(150, 100, 200, 25);
        add(txtPrimerApellido);

        JLabel l4 = new JLabel("Salario Base:");
        l4.setBounds(20, 140, 120, 25);
        add(l4);
        txtSalario = new JTextField();
        txtSalario.setBounds(150, 140, 200, 25);
        add(txtSalario);

        btnOk = new JButton("Guardar");
        btnOk.setBounds(150, 180, 90, 25);
        add(btnOk);

        btnCancel = new JButton("Cancelar");
        btnCancel.setBounds(260, 180, 90, 25);
        add(btnCancel);

        // Si estás editando, carga los valores existentes
        if (existing != null) {
            txtDpi.setText(String.valueOf(existing.getDpi()));
            txtDpi.setEnabled(false); // no cambiar DPI al editar
            txtPrimerNombre.setText(existing.getPrimerNombre());
            txtPrimerApellido.setText(existing.getPrimerApellido());
            txtSalario.setText(existing.getSalarioBase() == null ? "" : existing.getSalarioBase().toString());
        }

        // Acción del botón Guardar
        btnOk.addActionListener(e -> {
            try {
                Registro r = new Registro();
                r.setDpi(Long.parseLong(txtDpi.getText().trim()));
                r.setPrimerNombre(txtPrimerNombre.getText().trim());
                r.setPrimerApellido(txtPrimerApellido.getText().trim());
                if (!txtSalario.getText().trim().isEmpty()) {
                    r.setSalarioBase(new BigDecimal(txtSalario.getText().trim()));
                }
                this.registro = r;
                setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en los datos: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> {
            registro = null;
            setVisible(false);
        });
    }

    public Registro getRegistro() {
        return registro;
    }
}

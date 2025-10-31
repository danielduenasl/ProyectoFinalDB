/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.umg.proyectofinaldb;

import com.umg.proyectofinaldb.db.DBConnectionManager;
import com.umg.proyectofinaldb.db.RecordDAO;
import com.umg.proyectofinaldb.model.Registro;
import com.umg.proyectofinaldb.sync.SyncService;
import com.umg.proyectofinaldb.ui.RegistroFormDialog;
import com.umg.proyectofinaldb.util.FileLogger;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
/**
 *
 * @author PC
 */
public class MainFrame extends javax.swing.JFrame {

    // ====== Servicios / estado ======
    private DBConnectionManager dbManager;
    private DataSource dsSql;
    private DataSource dsOracle;
    private RecordDAO registroDAO;
    private FileLogger fileLogger;
    private SyncService syncService;

    private DefaultTableModel modelSql, modelOra;

    public MainFrame() {
        setTitle("SyncApp");
        setSize(1050, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ====== Inicializa servicios ======
        dbManager   = new DBConnectionManager();
        registroDAO = new RecordDAO();
        fileLogger  = new FileLogger("./bitacora.txt");
        syncService = new SyncService(fileLogger);

        // Carga properties (imprime DEBUG de URLs si está correcto)
        try { dbManager.loadFromProperties(); } catch (Exception e) { e.printStackTrace(); }

        // ====== Construye UI ======
        initComponents();
        modelSql = (DefaultTableModel) tblSql.getModel();
        modelOra = (DefaultTableModel) tblOra.getModel();
        addButtonActions();
        updateUiStates(false, false);
    }

    private void addButtonActions() {
        // Para verificar que llega el click
        btnConnectSql.addActionListener(e -> { System.out.println("CLICK Conectar SQL"); connectSql(); });
        btnDisconnectSql.addActionListener(e -> disconnectSql());
        btnRefreshSql.addActionListener(e -> loadSqlTable());
        btnCreateSql.addActionListener(e -> createRegistro(dsSql, "SQLSERVER"));
        btnEditSql.addActionListener(e -> editRegistro(dsSql, "SQLSERVER", tblSql, modelSql));
        btnDeleteSql.addActionListener(e -> deleteRegistro(dsSql, "SQLSERVER", tblSql, modelSql));

        btnConnectOra.addActionListener(e -> { System.out.println("CLICK Conectar ORA"); connectOra(); });
        btnDisconnectOra.addActionListener(e -> disconnectOra());
        btnRefreshOra.addActionListener(e -> loadOraTable());
        btnCreateOra.addActionListener(e -> createRegistro(dsOracle, "ORACLE"));
        btnEditOra.addActionListener(e -> editRegistro(dsOracle, "ORACLE", tblOra, modelOra));
        btnDeleteOra.addActionListener(e -> deleteRegistro(dsOracle, "ORACLE", tblOra, modelOra));

        btnSync.addActionListener(e -> syncDatabases());
    }

    // ================= Conexión =================
    private void connectSql() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    dsSql = dbManager.createSqlServerDataSource();
                    lblStatusSql.setText("Conectado");
                    appendLog("SQL Server conectado.");
                    loadSqlTable();
                    updateUiStates(dsSql != null, dsOracle != null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendLog("Error conectar SQL: " + ex.getMessage());
                    lblStatusSql.setText("Error");
                }
                return null;
            }
        }.execute();
    }

    private void disconnectSql() {
        try {
            if (dsSql != null) {
                dbManager.closeAll(); // si prefieres, crea un closeSoloSql() en DBConnectionManager
                dsSql = null;
                lblStatusSql.setText("No conectado");
                modelSql.setRowCount(0);
                appendLog("SQL Server desconectado.");
                updateUiStates(false, dsOracle != null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void connectOra() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    dsOracle = dbManager.createOracleDataSource();
                    lblStatusOra.setText("Conectado");
                    appendLog("Oracle conectado.");
                    loadOraTable();
                    updateUiStates(dsSql != null, dsOracle != null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendLog("Error conectar Oracle: " + ex.getMessage());
                    lblStatusOra.setText("Error");
                }
                return null;
            }
        }.execute();
    }

    private void disconnectOra() {
        try {
            if (dsOracle != null) {
                dbManager.closeAll(); // si prefieres, crea un closeSoloOracle() en DBConnectionManager
                dsOracle = null;
                lblStatusOra.setText("No conectado");
                modelOra.setRowCount(0);
                appendLog("Oracle desconectado.");
                updateUiStates(dsSql != null, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= Cargar tablas =================
    private void loadSqlTable() {
        if (dsSql == null) { modelSql.setRowCount(0); return; }
        new SwingWorker<List<Registro>, Void>() {
            @Override protected List<Registro> doInBackground() throws Exception { return registroDAO.getAll(dsSql); }
            @Override protected void done() {
                try {
                    List<Registro> list = get();
                    modelSql.setRowCount(0);
                    for (Registro r : list) {
                        modelSql.addRow(new Object[]{
                            r.getDpi(),
                            r.getPrimerNombre(),
                            r.getSegundoNombre(),
                            r.getPrimerApellido(),
                            r.getSegundoApellido(),
                            r.getDireccionDomiciliar(),
                            r.getTelefonoCasa(),
                            r.getTelefonoMovil(),
                            r.getSalarioBase(),
                            r.getBonificacion()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendLog("Error cargar SQL: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadOraTable() {
        if (dsOracle == null) { modelOra.setRowCount(0); return; }
        new SwingWorker<List<Registro>, Void>() {
            @Override protected List<Registro> doInBackground() throws Exception { return registroDAO.getAll(dsOracle); }
            @Override protected void done() {
                try {
                    List<Registro> list = get();
                    modelOra.setRowCount(0);
                    for (Registro r : list) {
                        modelOra.addRow(new Object[]{
                            r.getDpi(),
                            r.getPrimerNombre(),
                            r.getSegundoNombre(),
                            r.getPrimerApellido(),
                            r.getSegundoApellido(),
                            r.getDireccionDomiciliar(),
                            r.getTelefonoCasa(),
                            r.getTelefonoMovil(),
                            r.getSalarioBase(),
                            r.getBonificacion()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendLog("Error cargar ORA: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ================= CRUD =================
    private void createRegistro(DataSource ds, String source) {
        if (ds == null) { appendLog(source + " no conectado"); return; }
        RegistroFormDialog dlg = new RegistroFormDialog(this, null);
        dlg.setVisible(true);
        Registro r = dlg.getRegistro();
        if (r != null) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    try {
                        registroDAO.insert(ds, r);
                        fileLogger.log(source, "CREATE", r.getDpi(), "{}", "APPLIED");
                        appendLog(source + " - Registro creado DPI=" + r.getDpi());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fileLogger.log(source, "CREATE", r.getDpi(), ex.getMessage(), "FAILED");
                        appendLog(source + " - Error crear: " + ex.getMessage());
                    }
                    return null;
                }
                @Override protected void done() {
                    if ("SQLSERVER".equals(source)) loadSqlTable(); else loadOraTable();
                }
            }.execute();
        }
    }

    private void editRegistro(DataSource ds, String source, JTable table, DefaultTableModel model) {
        if (ds == null) { appendLog(source + " no conectado"); return; }
        int sel = table.getSelectedRow();
        if (sel < 0) { appendLog("Selecciona registro " + source); return; }
        long dpi = ((Number) table.getValueAt(sel, 0)).longValue();
        try {
            Registro r = registroDAO.getByDpi(ds, dpi);
            if (r == null) { appendLog("Registro no encontrado"); return; }
            RegistroFormDialog dlg = new RegistroFormDialog(this, r);
            dlg.setVisible(true);
            Registro updated = dlg.getRegistro();
            if (updated != null) {
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() {
                        try {
                            registroDAO.update(ds, updated);
                            fileLogger.log(source, "UPDATE", updated.getDpi(), "{}", "APPLIED");
                            appendLog(source + " - Registro actualizado DPI=" + updated.getDpi());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            fileLogger.log(source, "UPDATE", updated.getDpi(), ex.getMessage(), "FAILED");
                            appendLog(source + " - Error actualizar: " + ex.getMessage());
                        }
                        return null;
                    }
                    @Override protected void done() {
                        if ("SQLSERVER".equals(source)) loadSqlTable(); else loadOraTable();
                    }
                }.execute();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteRegistro(DataSource ds, String source, JTable table, DefaultTableModel model) {
        if (ds == null) { appendLog(source + " no conectado"); return; }
        int sel = table.getSelectedRow();
        if (sel < 0) { appendLog("Selecciona registro " + source); return; }
        long dpi = ((Number) table.getValueAt(sel, 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, "Eliminar DPI=" + dpi + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    registroDAO.delete(ds, dpi);
                    fileLogger.log(source, "DELETE", dpi, "{}", "APPLIED");
                    appendLog(source + " - Registro eliminado DPI=" + dpi);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fileLogger.log(source, "DELETE", dpi, ex.getMessage(), "FAILED");
                    appendLog(source + " - Error eliminar: " + ex.getMessage());
                }
                return null;
            }
            @Override protected void done() {
                if ("SQLSERVER".equals(source)) loadSqlTable(); else loadOraTable();
            }
        }.execute();
    }

    // ================= Sincronización =================
    private void syncDatabases() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    appendLog("Iniciando sincronización...");
                    if (dsSql == null) {
                        try { dsSql = dbManager.createSqlServerDataSource(); lblStatusSql.setText("Conectado"); appendLog("SQL conectado (auto)."); }
                        catch (Exception ex) { appendLog("No se pudo conectar SQL: " + ex.getMessage()); }
                    }
                    if (dsOracle == null) {
                        try { dsOracle = dbManager.createOracleDataSource(); lblStatusOra.setText("Conectado"); appendLog("Oracle conectado (auto)."); }
                        catch (Exception ex) { appendLog("No se pudo conectar Oracle: " + ex.getMessage()); }
                    }
                    if (dsSql == null || dsOracle == null) {
                        appendLog("Sincronización cancelada: faltan conexiones.");
                        updateUiStates(dsSql != null, dsOracle != null);
                        return null;
                    }
                    syncService.sincronizar(dsSql, "SQLSERVER", dsOracle, "ORACLE");
                    appendLog("Sincronización finalizada.");
                    loadSqlTable();
                    loadOraTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendLog("Error sincronizar: " + ex.getMessage());
                }
                updateUiStates(dsSql != null, dsOracle != null);
                return null;
            }
        }.execute();
    }

    // ===== util =====
    private void appendLog(String s) {
        System.out.println(s); // también a consola
        if (txtLog != null) txtLog.append(s + "\n");
    }

    private void updateUiStates(boolean sqlConnected, boolean oraConnected) {
        btnDisconnectSql.setEnabled(sqlConnected);
        btnCreateSql.setEnabled(sqlConnected);
        btnEditSql.setEnabled(sqlConnected);
        btnDeleteSql.setEnabled(sqlConnected);
        btnRefreshSql.setEnabled(sqlConnected);

        btnDisconnectOra.setEnabled(oraConnected);
        btnCreateOra.setEnabled(oraConnected);
        btnEditOra.setEnabled(oraConnected);
        btnDeleteOra.setEnabled(oraConnected);
        btnRefreshOra.setEnabled(oraConnected);

        // permitir presionar Sync aunque falte una; intentará auto-conectar
        btnSync.setEnabled(sqlConnected || oraConnected);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblOra = new javax.swing.JTable();
        btnConnectOra = new javax.swing.JButton();
        btnDisconnectOra = new javax.swing.JButton();
        btnCreateOra = new javax.swing.JButton();
        btnEditOra = new javax.swing.JButton();
        btnDeleteOra = new javax.swing.JButton();
        btnRefreshOra = new javax.swing.JButton();
        lblStatusOra = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblStatusSql = new javax.swing.JLabel();
        btnConnectSql = new javax.swing.JButton();
        btnDisconnectSql = new javax.swing.JButton();
        btnCreateSql = new javax.swing.JButton();
        btnEditSql = new javax.swing.JButton();
        btnDeleteSql = new javax.swing.JButton();
        btnRefreshSql = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSql = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnSync = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        tblOra.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "DPI", "Primer Nombre", "Segundo Nombre", "Primer Apellido", "Segundo Apellido", "Direccion", "Telefono Casa", "Telefono Movil", "Salario", "Bonificacion"
            }
        ));
        jScrollPane2.setViewportView(tblOra);

        btnConnectOra.setText("Conectar");

        btnDisconnectOra.setText("Desconectar");

        btnCreateOra.setText("Crear");

        btnEditOra.setText("Editar");

        btnDeleteOra.setText("Eliminar");

        btnRefreshOra.setText("Refrescar");

        lblStatusOra.setText("\"No Conectado\"");
        lblStatusOra.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("ORACLE");

        jLabel3.setIcon(new javax.swing.ImageIcon("C:\\Users\\PC\\Documents\\Progra\\ProyectoFinalDB\\ProyectoFinalDB\\src\\main\\java\\com\\umg\\proyectofinaldb\\images\\oracle (2).png")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnCreateOra)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditOra)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDeleteOra)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefreshOra))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnConnectOra)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDisconnectOra)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblStatusOra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addGap(51, 51, 51)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConnectOra, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDisconnectOra, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStatusOra, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateOra, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditOra, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteOra, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefreshOra, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));

        lblStatusSql.setText("\"No conectado\"");
        lblStatusSql.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnConnectSql.setText("Conectar");
        btnConnectSql.setBorder(null);

        btnDisconnectSql.setText("Desconectar");

        btnCreateSql.setText("Crear");

        btnEditSql.setText("Editar");

        btnDeleteSql.setText("Eliminar");

        btnRefreshSql.setText("Refrescar");

        tblSql.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "DPI", "Primer Nombre", "Segundo Nombre", "Primer Apellido", "Segundo Apellido", "Direccion", "Telefono Casa", "Telefono Movil", "Salario", "Bonificacion"
            }
        ));
        jScrollPane1.setViewportView(tblSql);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("SQL SERVER");

        jLabel4.setIcon(new javax.swing.ImageIcon("C:\\Users\\PC\\Documents\\Progra\\ProyectoFinalDB\\ProyectoFinalDB\\src\\main\\java\\com\\umg\\proyectofinaldb\\images\\sqlserver (2).png")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnCreateSql)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditSql)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDeleteSql)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefreshSql))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnConnectSql, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(btnDisconnectSql)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblStatusSql, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConnectSql, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDisconnectSql, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStatusSql, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateSql, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditSql, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteSql, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefreshSql, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnSync.setText("Sincronizar");

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane3.setViewportView(txtLog);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnSync, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSync, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConnectOra;
    private javax.swing.JButton btnConnectSql;
    private javax.swing.JButton btnCreateOra;
    private javax.swing.JButton btnCreateSql;
    private javax.swing.JButton btnDeleteOra;
    private javax.swing.JButton btnDeleteSql;
    private javax.swing.JButton btnDisconnectOra;
    private javax.swing.JButton btnDisconnectSql;
    private javax.swing.JButton btnEditOra;
    private javax.swing.JButton btnEditSql;
    private javax.swing.JButton btnRefreshOra;
    private javax.swing.JButton btnRefreshSql;
    private javax.swing.JButton btnSync;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblStatusOra;
    private javax.swing.JLabel lblStatusSql;
    private javax.swing.JTable tblOra;
    private javax.swing.JTable tblSql;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}

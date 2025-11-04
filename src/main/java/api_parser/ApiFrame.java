package api_parser;

import api_parser.model.GenerateResponse;
import api_parser.model.RequestSource;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.IHttpService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ApiFrame extends JFrame {

    private List<IHttpRequestResponse> reqList;
    private IExtensionHelpers helpers;
    private IBurpExtenderCallbacks callbacks;

    private JTextField collectionNameInput;
    private JTextField folderNameInput;
    private JComboBox<DocTypeOption> doctypeDropdown;
    private ApiTable tableModel;

    PrintWriter stdout;
    PrintWriter stderr;

    private int reqCount;


    public ApiFrame(int reqCount,PrintWriter stdout,PrintWriter stderr) {

        this.stdout = stdout;
        this.stderr = stderr;
        this.setBounds(20, 20, 1200, 540);
        this.reqCount = reqCount;
        render();
    }

    private void render() {
        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(null);



        //Title Label
        JLabel titleLabel = new JLabel("API Exporter");
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 15));
        titleLabel.setBounds(10,10,284,23);
        panel.add(titleLabel);


        // Collection Name
        JLabel collectionNameLabel = new JLabel("Collection Name:");
        collectionNameLabel.setFont(new Font("Consolas",Font.PLAIN,12));
        collectionNameLabel.setBounds(30,40,100,25);


        collectionNameInput = new JTextField();
        collectionNameInput.setBounds(150,40,300,25);
        panel.add(collectionNameLabel);
        panel.add(collectionNameInput);

        // Folder Name
        JLabel folderNameLabel = new JLabel("Folder Name:");
        folderNameLabel.setFont(new Font("Consolas",Font.PLAIN,12));
        folderNameLabel.setBounds(30,80,100,25);


        folderNameInput = new JTextField();
        folderNameInput.setBounds(150,80,300,25);

        JButton setFolderNameBtn = new JButton("Fill All Folder");
        setFolderNameBtn.setFont(new Font("Consolas",Font.PLAIN,12));
        setFolderNameBtn.setBounds(475,80,200,25);

        setFolderNameBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < reqCount; i++)
                    tableModel.setValueAt(folderNameInput.getText(), i, ApiTable.FOLDER_NAME_INDEX);
            }
        });

        panel.add(folderNameLabel);
        panel.add(folderNameInput);
        panel.add(setFolderNameBtn);

        // DocType Name
        JLabel docTypeLabel = new JLabel("DocType:");
        docTypeLabel.setFont(new Font("Consolas",Font.PLAIN,12));
        docTypeLabel.setBounds(30,120,100,25);

        DocTypeOption[] docTypes = new DocTypeOption[]{
                new DocTypeOption("postman-v2.1", "Postman v2.1", ".json"),
                new DocTypeOption("openapi-v3.0", "OpenAPI v3.0 (JSON)", ".json"),
                new DocTypeOption("openapi-v3.1", "OpenAPI v3.1 (JSON)", ".json")
        };
        doctypeDropdown = new JComboBox<>(docTypes);
        doctypeDropdown.setBounds(150,120,180,25);

        panel.add(docTypeLabel);
        panel.add(doctypeDropdown);

        // DocType Name
        // Tablodaki verilerin benzersizliğini kontrol etmek için JCheckBox oluştur
        JCheckBox uniqueCheckbox = new JCheckBox("Export Unique URLs Only");
        uniqueCheckbox.setFont(new Font("Consolas",Font.PLAIN,12));
        uniqueCheckbox.setBounds(30,160,200,25);
        panel.add(uniqueCheckbox);

        // JTable ve modelini oluştur
        tableModel = new ApiTable(this.reqCount);
        JTable requestsTable = new JTable(tableModel);


        requestsTable.getColumnModel().getColumn(ApiTable.FOLDER_NAME_INDEX).setPreferredWidth(100);
        requestsTable.getColumnModel().getColumn(ApiTable.NAME_COLUMN_INDEX).setPreferredWidth(150);
        requestsTable.getColumnModel().getColumn(ApiTable.METHOD_COLUMN_INDEX).setPreferredWidth(75);
        requestsTable.getColumnModel().getColumn(ApiTable.URL_COLUMN_INDEX).setPreferredWidth(425);



        // JTable'ı kaydırma paneline ekle
        JScrollPane reqScrollPane = new JScrollPane(requestsTable);
        reqScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        reqScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        reqScrollPane.setBounds(10, 200, 750, 250); // x=200, y=200

        panel.add(reqScrollPane);

        // Export butonu
        JButton exportButton = new JButton("Export");

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RequestSource> reqSrcList = new ArrayList<>();
                for (int i = 0; i < reqList.size(); i++) {
                    RequestSource rs = new RequestSource(reqList.get(i),
                            (String) tableModel.getValueAt(i, ApiTable.NAME_COLUMN_INDEX),
                            (String) tableModel.getValueAt(i, ApiTable.FOLDER_NAME_INDEX));
                    reqSrcList.add(rs);
                }
                Generator generator = new Generator(reqSrcList, callbacks, "utf-8", stdout, stderr);
                try {
                    boolean isSelected = uniqueCheckbox.isSelected();
                    DocTypeOption selectedDocType = (DocTypeOption) doctypeDropdown.getSelectedItem();
                    if (selectedDocType == null) {
                        stdout.println("Error: No document type selected");
                        callbacks.issueAlert("No document type selected");
                        return;
                    }

                    GenerateResponse resp = generator.generate(selectedDocType.getId(), getCollectionName(),isSelected);

                    // Hata kontrolü
                    if (resp.getStatus() == null || !resp.getStatus()) {
                        stdout.println("Error: " + resp.getMessage());
                        callbacks.issueAlert(
                                "An error occurred: " + resp.getMessage());
                    } else {
                        File outputFile = promptForDestination(selectedDocType, getCollectionName());
                        if (outputFile == null) {
                            stdout.println("Export cancelled by user");
                            return;
                        }

                        if (outputFile.exists()) {
                            int overwriteConfirmation = JOptionPane.showConfirmDialog(
                                    resolveParentWindow(),
                                    "File already exists. Do you want to replace it?",
                                    "File Exists",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );

                            if (overwriteConfirmation != JOptionPane.YES_OPTION) {
                                stdout.println("Export aborted to avoid overwriting existing file");
                                return;
                            }
                        }

                        try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
                            writer.write(resp.getMessage());
                            stdout.println("File saved successfully: " + outputFile.getAbsolutePath());
                            JOptionPane.showMessageDialog(resolveParentWindow(),
                                    "File saved successfully: " + outputFile.getAbsolutePath(),
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ioEx) {
                            stdout.println("Failed to save file: " + ioEx.getMessage());
                            callbacks.issueAlert(
                                    "File save error: " + ioEx.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    stdout.println("Exception occurred: " + ex.getMessage());
                    callbacks.issueAlert(
                            "An unexpected error occurred. Please check Burp Extensions Errors tab. Message: " + ex.getMessage());
                }
            }
        });

        exportButton.setFont(new Font("Consolas",Font.PLAIN,12));
        exportButton.setBounds(630,475,100,25);

        panel.add(exportButton);

        // Ana paneli çerçeveye ekle
        add(panel);

        // Çerçeve ayarları
        setTitle("API Export Settings");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public void setRequest(List<IHttpRequestResponse> reqList, IBurpExtenderCallbacks callbacks) {
        this.reqList = reqList;
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        for (int i = 0; i < reqList.size(); i++) {
            IHttpRequestResponse reqRes = reqList.get(i);
            IRequestInfo iReqInfo = helpers.analyzeRequest(reqRes.getRequest());
            String name = String.format("%03d", i + 1);
            String method = iReqInfo.getMethod();
            String url = getURL(reqRes);

            /* set value on table */
            this.tableModel.setValueAt(name, i, ApiTable.NAME_COLUMN_INDEX);
            this.tableModel.setValueAt(method, i, ApiTable.METHOD_COLUMN_INDEX);
            this.tableModel.setValueAt(url, i, ApiTable.URL_COLUMN_INDEX);
            // this.tableModel.setValueAt(true, i,
            // PostmanTableModel.ENABLED_COLUMN_INDEX);
        }
    }


    private String getURL(IHttpRequestResponse requestResponse) {
        IHttpService iHS = requestResponse.getHttpService();
        String port = Integer.toString(iHS.getPort());
        String urlPort;
        if (port.equals("80") || port.equals("443"))
            urlPort = "";
        else
            urlPort = ":" + port;

        String url;
        String uri = new String(requestResponse.getRequest()).split("\n")[0].split(" ")[1];
        if (uri.startsWith("http"))
            url = uri;
        else
            url = iHS.getProtocol() + "://" + iHS.getHost() + urlPort
                    + new String(requestResponse.getRequest()).split("\n")[0].split(" ")[1];

        return url;
    }

    // Getter metotları
    public String getCollectionName() {
        return collectionNameInput.getText();
    }

    public String getFolderName() {
        return folderNameInput.getText();
    }

    public String getSelectedEncoding() {
        DocTypeOption selected = (DocTypeOption) doctypeDropdown.getSelectedItem();
        return selected != null ? selected.getId() : null;
    }

    public ApiTable getTableModel() {
        return this.tableModel;
    }

    private File promptForDestination(DocTypeOption docType, String collectionName) {
        String safeName = sanitizeFileName(collectionName);
        String extension = docType.getDefaultExtension();
        String defaultFile = extension.isEmpty() ? safeName : safeName + extension;

        FileDialog dialog = new FileDialog(resolveParentFrame(), "Select File to Save Generated Output", FileDialog.SAVE);
        dialog.setFile(defaultFile);
        dialog.setVisible(true);
        try {
            String directory = dialog.getDirectory();
            String file = dialog.getFile();
            if (directory == null || file == null) {
                return null;
            }

            File chosen = new File(directory, file);
            if (!extension.isEmpty() && !chosen.getName().contains(".")) {
                chosen = new File(chosen.getParentFile(), chosen.getName() + extension);
            }

            return chosen;
        } finally {
            dialog.dispose();
        }
    }

    private String sanitizeFileName(String value) {
        String candidate = (value == null || value.trim().isEmpty()) ? "api-export" : value.trim();
        return candidate.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private Frame resolveParentFrame() {
        Frame frame = resolveFrameFromMontoya();
        if (frame != null) {
            return frame;
        }
        Window window = resolveParentWindow();
        return window instanceof Frame ? (Frame) window : null;
    }

    private Window resolveParentWindow() {
        Window window = SwingUtilities.getWindowAncestor(this.getContentPane());
        if (window != null) {
            return window;
        }
        for (Frame candidate : Frame.getFrames()) {
            if (candidate != null && candidate.isVisible()) {
                return candidate;
            }
        }
        return null;
    }

    private Frame resolveFrameFromMontoya() {
        if (callbacks == null) {
            return null;
        }
        try {
            Method montoyaMethod = callbacks.getClass().getMethod("montoyaApi");
            Object montoya = montoyaMethod.invoke(callbacks);
            if (montoya == null) {
                return null;
            }
            Method userInterfaceMethod = montoya.getClass().getMethod("userInterface");
            Object userInterface = userInterfaceMethod.invoke(montoya);
            if (userInterface == null) {
                return null;
            }
            Method swingUtilsMethod = userInterface.getClass().getMethod("swingUtils");
            Object swingUtils = swingUtilsMethod.invoke(userInterface);
            if (swingUtils == null) {
                return null;
            }
            Method suiteFrameMethod = swingUtils.getClass().getMethod("suiteFrame");
            Object frame = suiteFrameMethod.invoke(swingUtils);
            if (frame instanceof Frame) {
                return (Frame) frame;
            }
        } catch (Exception ignored) {
            // Montoya API not available; fall back to Swing heuristics.
        }
        return null;
    }
}

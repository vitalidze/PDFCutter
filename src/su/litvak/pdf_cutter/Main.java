package su.litvak.pdf_cutter;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

public class Main extends JFrame implements ActionListener {
    JPanel jpMain = new JPanel();

    JLabel lblPages = new JLabel("Enter page(s) to extract");
    JTextField fldPages = new JTextField();
    JTextField fldPath = new JTextField();
    JButton btnBrowse = new JButton("Browse...");
    JButton btnExtract = new JButton("Extract...");

    FileFilter pdfFileFilter = new FileFilter()
    {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
        }

        @Override
        public String getDescription() {
            return "Portable Document Format (PDF) files";
        }
    };

    public Main() {
        setTitle("PDF cutter");

        GroupLayout gl = new GroupLayout(jpMain);
        gl.setHorizontalGroup(gl.createParallelGroup()
            .addGroup(gl.createSequentialGroup()
                .addComponent(fldPath)
                .addComponent(btnBrowse)
            )
            .addGroup(gl.createSequentialGroup()
                .addComponent(lblPages)
                .addComponent(fldPages)
                .addComponent(btnExtract)
            )
        );
        gl.setVerticalGroup(gl.createSequentialGroup()
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(fldPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse)
            )
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lblPages)
                    .addComponent(fldPages, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExtract)
            )
        );
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);
        gl.linkSize(btnBrowse, btnExtract);

        jpMain.setLayout(gl);

        getContentPane().add(jpMain);

        fldPath.setEditable(false);
        btnExtract.setEnabled(false);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        /**
         * Subscribe
         */
        btnBrowse.addActionListener(this);
        btnExtract.addActionListener(this);

        /**
         * Show frame in the middle
         */
        int w = 400;
        int h = 120;
        int sW = getToolkit().getScreenSize().width;
        int sH = getToolkit().getScreenSize().height;

        setBounds((sW - w) / 2, (sH - h) / 2, w, h);
        setVisible(true);
    }

    public static void main(String... args) {
        new Main();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBrowse) {
            JFileChooser jfc = new JFileChooser(System.getProperty("user.home"));
            jfc.setFileFilter(pdfFileFilter);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fldPath.setText(jfc.getSelectedFile().getAbsolutePath());
                btnExtract.setEnabled(true);
            }
        } else if (e.getSource() == btnExtract) {
            File inputFile = new File(fldPath.getText());
            if (!inputFile.exists()) {
                JOptionPane.showMessageDialog(this, "File '" + inputFile.getName() + "' does not exist.", "Unable to find input file", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser jfc = new JFileChooser(inputFile.getParent());
            jfc.setFileFilter(pdfFileFilter);

            if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    PdfReader reader = new PdfReader(inputFile.getAbsolutePath());
                    int n = reader.getNumberOfPages();

                    Document document = new Document(reader.getPageSizeWithRotation(1));
                    PdfCopy writer = new PdfCopy(document, new FileOutputStream(jfc.getSelectedFile()));
                    document.open();

                    StringTokenizer st = new StringTokenizer(fldPages.getText(), ",");
                    while (st.hasMoreTokens()) {
                        String next = st.nextToken();

                        if (next.indexOf('-') > 0) {
                            String[] splitted = next.split("-");
                            if (splitted.length > 2) {
                                throw new IllegalArgumentException("Wrong range format: " + next);
                            }

                            int start = Integer.parseInt(splitted[0].trim());
                            int end = Integer.parseInt(splitted[1].trim());

                            for (int pageIndex = start; pageIndex <= end; pageIndex++) {
                                PdfImportedPage page = writer.getImportedPage(reader, pageIndex);
                                writer.addPage(page);
                            }
                        } else {
                            int pageIndex = Integer.parseInt(next.trim());
                            PdfImportedPage page = writer.getImportedPage(reader, pageIndex);
                            writer.addPage(page);
                        }
                    }

                    document.close();
                    writer.close();

                    JOptionPane.showMessageDialog(this, "Result PDF saved to " + jfc.getSelectedFile().getName(), "Work done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error occurred", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}

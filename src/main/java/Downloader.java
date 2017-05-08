import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.omg.SendingContext.RunTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

public class Downloader implements ActionListener {

    // normal variable
    private static String urlStr;
    private static String fileName;
    private static int partNum;

    // ui fields
    private TextField urlField;
    private TextField fileNameFiled;
    private TextField threadNumField;
    private JTextField uaField;
    private JProgressBar progressBar;
    private JComboBox<Object> uaComboBox;

    private DownloadThread[] downloadThreads;

    public static void main(String[] args) {
        Downloader downloader = new Downloader();
        downloader.jframe();
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                downloader.updateProgress();
            }
        }, 0, 1000);

        // reset the properties
        try {
            downloader.resetProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void jframe() {
        JFrame jframe = new JFrame("Mdowner");
        JPanel jpanel = new JPanel();
        // information
        JLabel infoLabel = new JLabel("多线程下载器");
        // url
        JLabel urlLabel = new JLabel("URL:       ");
        urlField = new TextField("http://www.baidu.com", 60);
        Box urlBox = Box.createHorizontalBox();
        urlBox.add(urlLabel);
        urlBox.add(urlField);
        // file name
        JLabel fileNameLabel = new JLabel("文件名:  ");
        fileNameFiled = new TextField("C://baidu.html",  60);
        Box fileNameBox = Box.createHorizontalBox();
        fileNameBox.add(fileNameLabel);
        fileNameBox.add(fileNameFiled);
        // thread num
        JLabel threadNumLabel = new JLabel("线程数:  ");
        threadNumField = new TextField("4",  60);
        Box threadNumBox = Box.createHorizontalBox();
        threadNumBox.add(threadNumLabel);
        threadNumBox.add(threadNumField);
        // User Agent
        JLabel uaLabel = new JLabel("下载UA:  ");
        uaComboBox = new JComboBox<>(UAConstants.getUAMap().keySet().toArray());
        uaComboBox.addItem("自定义");
        uaField = new JTextField("", 60);
        uaComboBox.addActionListener((ActionEvent e) -> {
            //JComboBox<Object> comboBox = (JComboBox<Object>) e.getSource();
            String selectedName = (String) uaComboBox.getSelectedItem();
            String uaValue = UAConstants.getUAMap().get(selectedName);
            uaField.setText(uaValue);
        });
        Box uaBox = Box.createHorizontalBox();
        uaBox.add(uaLabel);
        uaBox.add(uaComboBox);
        uaBox.add(uaField);

        // progress bar
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("下载进度: 0/0");

        // buttons
        Box bBox = Box.createHorizontalBox();
        JButton dButton = new JButton("下载");
        JButton pButton = new JButton("暂停");
        JButton rButton = new JButton("删除");
        bBox.add(dButton);
        bBox.add(pButton);
        bBox.add(rButton);
        dButton.addActionListener(this);
        pButton.addActionListener(this);
        rButton.addActionListener(this);

        jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.Y_AXIS));
        jpanel.add(infoLabel);
        jpanel.add(urlBox);
        jpanel.add(fileNameBox);
        jpanel.add(threadNumBox);
        jpanel.add(uaBox);
        jpanel.add(progressBar);
        jpanel.add(bBox);


        jframe.add(jpanel);
        jframe.pack();
        jframe.setVisible(true);
        jframe.setResizable(false);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    saveProperties();
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
        });
    }


    @Override
    public void actionPerformed(ActionEvent event) {
        urlStr = urlField.getText();
        fileName = fileNameFiled.getText();

        switch (event.getActionCommand()) {
            case "下载":
                startDownload(event);
                break;
            case "暂停":
                if (downloadThreads != null) {
                }
                break;
            case "删除":
                if (downloadThreads != null) {
                    for (int i = 0; i < partNum; ++i) {
                        try {
                            downloadThreads[i].stopDownload();
                            downloadThreads[i].stop();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
                new File(fileName).delete();
                break;
        }
    }


    private void startDownload(ActionEvent event) {
        try {
            partNum = Integer.valueOf(threadNumField.getText());
        } catch (RuntimeException error) {
            progressBar.setString("下载提示：线程数不合理");
            return;
        }
        // init the downloadThreads
        if (downloadThreads == null) {
            downloadThreads = new DownloadThread[partNum];
        }

        if (new File(fileName).exists()) {
            progressBar.setString("文件已存在");
            return;
        }

        for (int i = 0; i < partNum; ++i) {
            downloadThreads[i] = new DownloadThread(urlStr, partNum, i, fileName, uaField.getText());
            downloadThreads[i].start();
        }

    }


    private void updateProgress() {
        if (downloadThreads == null) {
            return;
        }
        long contentLen = downloadThreads[0].getContentLength();   // KB
        int allDownloadedLen = 0;
        for (int i = 0; i < partNum; ++i) {
            long downloadedLen = downloadThreads[i].getDownloadedLength();
            allDownloadedLen += downloadedLen;
        }
        if (allDownloadedLen < contentLen) {
            progressBar.setString("下载进度: " + allDownloadedLen/1024 + "/" + contentLen/1024);
            progressBar.setValue(allDownloadedLen/1024);
            //设置progressBar的长度
            progressBar.setMaximum((int)(contentLen/1024));
        } else {
            progressBar.setString("下载完成");
        }
    }


    private void saveProperties() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("urlStr", urlField.getText());
        properties.setProperty("fileName", fileNameFiled.getText());
        properties.setProperty("partNum", threadNumField.getText());
        properties.setProperty("UA", (String)uaComboBox.getSelectedItem());
        properties.setProperty("uaValue", UAConstants.getUAMap().get(uaComboBox.getSelectedItem()));
        properties.store(new FileOutputStream("mdowner-settings.properties"), "Properties");
    }


    private void resetProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("mdowner-settings.properties"));
        urlField.setText(properties.getProperty("urlStr"));
        fileNameFiled.setText(properties.getProperty("fileName"));
        threadNumField.setText(properties.getProperty("partNum"));
        uaComboBox.setSelectedItem(properties.getProperty("UA"));
        uaField.setText(properties.getProperty("uaValue"));
    }
}

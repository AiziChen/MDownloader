import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

class DownloadThread extends Thread {
    private String urlStr;
    // 段大小
    private int partNum;
    // 部分
    private int part;
    // 保存的文件名
    private String fileName;
    private String ua;

    // 文件长度
    private long contentLen;

    // 下载长度
    int dlength = 0;

    private HttpURLConnection conn;
    private RandomAccessFile[] rafs;

    public DownloadThread(String urlStr, int partNum, int part, String fileName, String ua) {
        this.urlStr = urlStr;
        this.partNum = partNum;
        this.part = part;
        this.fileName = fileName;
        this.ua = ua;
        rafs = new RandomAccessFile[partNum];
    }

    @Override
    public void run() {
        download();
    }

    private void download() {
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            //conn.setReadTimeout(7 * 1000);
            conn.setRequestMethod("GET");
            conn.addRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-Type", "*/*");
            conn.addRequestProperty("User-Agent", ua);
            contentLen = conn.getContentLength();
            conn.connect();

            long currentPartSize = contentLen / partNum;
            long pos = currentPartSize * part;
            InputStream inStream = conn.getInputStream();
            inStream.skip(pos);
            rafs[part] = new RandomAccessFile(fileName, "rw");
            rafs[part].seek(pos);

            byte[] buff = new byte[1024];
            int hasRead;
            while ((hasRead=inStream.read(buff)) != -1 && dlength < currentPartSize) {
                rafs[part].write(buff, 0, hasRead);
                dlength += hasRead;
            }
            // Retrieve the resources
            rafs[part].close();
            inStream.close();
            conn.disconnect();
        } catch (IOException e) {
            System.out.println("下载文件出错了。");
            e.printStackTrace();
        }
    }

    public void stopDownload() {
        if (conn != null) {
            conn.disconnect();
            try {
                rafs[part].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public long getContentLength() {
        return this.contentLen;
    }

    public long getDownloadedLength() {
        return this.dlength;
    }
}
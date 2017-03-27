package cn.edu.hit.gpcs.area.util;

import cn.edu.hit.gpcs.utils.DotEnv;

import java.io.*;
import java.util.List;

/**
 * 文件读写工具
 */
public class FileUtils {
    private static String fileName;

    static {
        fileName = DotEnv.get("LOG_FILE_PATH");
    }

    public static void logError (Exception error, String appendMessage) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            if (appendMessage != null)
                fw.write(appendMessage);
            PrintWriter out = new PrintWriter(new BufferedWriter(fw));
            error.printStackTrace(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exportCsv(File file, List<String> dataList) {
        boolean isSucess = false;
        FileOutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out);
            bw = new BufferedWriter(osw);
            if (dataList != null && !dataList.isEmpty()) {
                for (String data : dataList) {
                    bw.append(data).append("\r\n");
                }
            }
            isSucess = true;
        } catch (Exception e) {
            isSucess = false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                    bw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                    osw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSucess;
    }

    /**
     * 缓存输出类
     */
//    public class BufferedOutputStream extends FilterOutputStream {
//        protected byte buf[]; // 建立写缓存区
//        int count = 0;
//        int fileendpos = 0;
//        public BufferedOutputStream(OutputStream out, int size) {
//            super(out);
//            if (size <= 0) {
//                throw new IllegalArgumentException("Buffer size <= 0");
//            }
//            buf = new byte[size];
//        }
//        public synchronized void write(int b) throws IOException {
//            if (count >= buf.length) {
//                flushBuffer();
//            }
//            buf[count++] = (byte)b; // 直接从BUF[]中读取
//        }
//        private void flushBuffer() throws IOException {
//            if (count > 0) {
//                out.write(buf, 0, count);
//                count = 0;
//            }
//        }
//        public boolean append(byte bw) throws IOException {
//            return this.write(bw, this.fileendpos + 1);
//        }
//    }
}

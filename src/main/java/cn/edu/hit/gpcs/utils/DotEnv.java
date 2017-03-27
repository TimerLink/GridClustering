package cn.edu.hit.gpcs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yuripourre
 * @link https://github.com/yuripourre/dotenv4J/blob/master/src/main/java/com/dotenv/DotEnv.java
 */
public class DotEnv {
    private static DotEnv instance;

    private static final String DOT_ENV_FILENAME = ".env";
    private Map<String, String> params = new HashMap<String, String>();

    private DotEnv() {
        loadParams(".");
    }

    public static DotEnv getInstance() {
        if (instance == null)
            instance = new DotEnv();
        return instance;
    }

    private void loadParams(String path) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(path, DOT_ENV_FILENAME)));
            String line;
            while(true) {
                line = br.readLine();
                if(line == null) {
                    break;
                }
                parseLine(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try{
                    br.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseLine(String line) {
        String[] parts = line.split("=");
        if (parts.length == 2) {
            params.put(fix(parts[0]), fix(parts[1]));
        }
    }

    private String fix(String in) {
        String out = in.trim();
        if (out.length() >= 2 && (out.matches("^\".*\"$") || out.matches("^'.*'$"))) {
            out = out.substring(1, out.length() - 1);
        }
        return out;
    }

    public static String get(String key) {
        return getInstance().params.get(key);
    }
}
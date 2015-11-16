package com.siat.msg.util;

import java.io.File;
import java.util.Arrays;

/**
 * @author Zhu Yingtao
 * @ClassName LineCounter
 * @Description TODO
 * @date 2015年3月16日 下午11:30:04
 */
public class LineCounter {

    public static void count(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            file.listFiles();
        } else {

        }
    }

    public static void count() {
        File file = new File("src");
        String[] files = file.list();
        for (int i = 0; i < files.length; i++) {
            File f = new File(files[i]);
            while (f.isDirectory()) {
                // countAll();
            }
        }
        System.out.println(Arrays.toString(files));
    }

    /**
     * @param args
     * @Title: main
     * @Description: TODO
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        count();
    }

}

package com.brandon.apps.groupstudio.assets;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 12/17/2015.
 */
public class FileHandler {
    public static void write(Context c, String data, String name) throws IOException {
        File directory = Environment.getExternalStoragePublicDirectory("/GroupStudioJSON");
        directory.mkdirs();
        File file = new File(directory, name + ".json");
        FileWriter writer = new FileWriter(file);
        writer.write(data);
        writer.flush();
        writer.close();
    }
    public static String read(Context c, String name) throws IOException {
        File directory = Environment.getExternalStoragePublicDirectory("/GroupStudioJSON");
        directory.mkdirs();
        File file = new File(directory, name);
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String data = "";
        int character;
        int counter = 0;
        while ((character = bufferedReader.read()) != -1) {
            data += (char)character;
            counter++;
        }
        return data;
    }
    public static List<File> getAll() {
        File directory = Environment.getExternalStoragePublicDirectory("/GroupStudioJSON");
        directory.mkdirs();
        File[] buffer = directory.listFiles();
        List<File> list = new ArrayList<File>();
        for (File file:
                buffer) {
            if (file.getName().substring(file.getName().lastIndexOf('.') + 1).equals("json")) {
                list.add(file);
            }
        }
        return list;
    }
    public static void init() {
        File directory = Environment.getExternalStoragePublicDirectory("/GroupStudioJSON");
        directory.mkdirs();
    }
}

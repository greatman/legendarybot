package com.greatmancode.legendarybot.commands.token;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Test {

    public static void main(String[] args) throws IOException {

        Manifest manifest = new JarFile("C:\\Users\\lebel\\legendarybot2\\dist\\plugins\\plugin-affixcommand.jar").getManifest();

        manifest.getEntries().forEach((k,v) -> {
            System.out.println(k + ":" + v);
        });
    }
}

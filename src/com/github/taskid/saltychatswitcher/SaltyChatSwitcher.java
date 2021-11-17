package com.github.taskid.saltychatswitcher; // SaltyChatSwitcher, 09:51 02.11.2021

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaltyChatSwitcher {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        try {
            while (true) {
                ProcessBuilder builder = new ProcessBuilder("tasklist.exe");
                Process process = builder.start();
                StringBuilder tasks = new StringBuilder();
                try (Scanner scanner = new Scanner(process.getInputStream())) {
                    while (scanner.hasNextLine()) {
                        tasks.append(scanner.nextLine());
                    }
                }
                if (tasks.toString().contains("ts3client")) {
                    int result = message("WARNING:\nTeamSpeak is still running.\nClose it before changing version.", JOptionPane.WARNING_MESSAGE, "Close", "Retry", "Proceed anyway");
                    if (result <= 0) {
                        return;
                    }
                    if (result == 2) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Throwable ex) {
            message("Error:\n" + ex.getMessage(), "Close");
            return;
        }

        File ts3ClientFolder = new File(System.getenv("APPDATA"), "TS3Client/");
        if(!ts3ClientFolder.exists() || !ts3ClientFolder.isDirectory()) {
            message(ts3ClientFolder.getAbsolutePath() + "\nfolder not found", "Close");
            return;
        }
        File addons = new File(ts3ClientFolder, "addons.ini");
        if(!addons.exists()) {
            message(addons.getAbsolutePath() + "\ndoesn't exist", "Close");
            return;
        }

        File switcherFolder = new File(ts3ClientFolder, "sc_switcher/");
        if(!switcherFolder.exists()) {
            if(!switcherFolder.mkdirs()) {
                message(switcherFolder.getAbsolutePath() + "\ncouldn't be created", "Close");
                return;
            }
        }

        File setupCompleted = new File(switcherFolder, ".setupCompleted");
        if(!setupCompleted.exists()) {
            int result;
            int firstPlugin, secondPlugin;

            result = message("Entered first setup. You only have to do this once.\n\nIMPORTANT:\nYou need both SaltyChat 2 and SaltyChat 3 installers (end \".ts3_plugin\").\nIf you don't have them, get them before continuing.\n\nWhat version of SaltyChat do you have installed currently?\n(You can check this in TeamSpeak: Tools > Options > Addons)", "SaltyChat 2", "SaltyChat 3", "Nothing");
            if(result == 0 || result == 2) {
                firstPlugin = 2;
                secondPlugin = 3;
            } else if (result == 1) {
                firstPlugin = 3;
                secondPlugin = 2;
            } else {
                return;
            }

            result = message("Make sure ONLY SaltyChat " + firstPlugin + " is installed and TS 3 isn't started. When done, click \"Next\".", "Next");

            if(result != 0) return;

            try {
                initCopySaltyChat(addons, ts3ClientFolder, new File(switcherFolder, "sc" + firstPlugin + "/"));
            } catch (Throwable ex) {
                message("Error:\n" + ex.getMessage(), "Close");
                return;
            }

            result = message("Successfully saved SaltyChat " + firstPlugin + " contents.\nNow, please uninstall SaltyChat " + firstPlugin + " and install SaltyChat " + secondPlugin + " and again make sure TS 3 isn't started. When done, click \"Next\".", "Next");
            if(result != 0) return;

            try {
                initCopySaltyChat(addons, ts3ClientFolder, new File(switcherFolder, "sc" + secondPlugin + "/"));
            } catch (Throwable ex) {
                message("Error:\n" + ex.getMessage(), "Close");
                return;
            }

            try {
                setupCompleted.createNewFile();
                message("Successfully saved SaltyChat " + secondPlugin + " contents.\nThe setup is now completed.", "Continue");
            } catch (Throwable ex) {
                message("Error:\n" + ex.getMessage(), "Close");
                return;
            }
        }

        int result = message("What do you want to install?",  JOptionPane.QUESTION_MESSAGE, "SaltyChat 2", "SaltyChat 3", "Nothing (Uninstall)");
        if(result == 0) { // SC 2
            try {
                copySaltyChatVersion(addons, ts3ClientFolder, new File(switcherFolder, "sc2/"));
                message("Success!\nInstalled SaltyChat v2.", JOptionPane.INFORMATION_MESSAGE, "Close");
            } catch (Throwable ex) {
                message("Error:\n" + ex.getMessage(), "Close");
            }
        } else if (result == 1) { // SC 3
            try {
                copySaltyChatVersion(addons, ts3ClientFolder, new File(switcherFolder, "sc3/"));
                message("Success!\nInstalled SaltyChat v3.", JOptionPane.INFORMATION_MESSAGE, "Close");
            } catch (Throwable ex) {
                message("Error:\n" + ex.getMessage(), "Close");
            }
        } else if (result == 2) {
            try {
                removeSaltyChat(addons, ts3ClientFolder);
                message("Success!\nUninstalled SaltyChat.", JOptionPane.INFORMATION_MESSAGE, "Close");
            } catch (Throwable ex) {
                message("Error:\n" + ex.getMessage(), "Close");
            }
        }

    }

    private static int message(String msg, String... options) {
        return message(msg, JOptionPane.PLAIN_MESSAGE, options);
    }

    private static int message(String msg, int messageType, String... options) {
        return JOptionPane.showOptionDialog(null, msg, "SaltyChatSwitcher v1.0.0 by TaskID",
                JOptionPane.DEFAULT_OPTION, messageType, null,
                options, options[0]);
    }

    private static void copySaltyChatVersion(File addonsFile, File ts3ClientFolder, File src) throws IOException {
        if(!src.exists()) throw new IOException(src.getAbsolutePath() + " not found");

        List<String> addonsLines = new ArrayList<>();
        try(Scanner scanner = new Scanner(new File(src, "addons_content"))) {
            while(scanner.hasNextLine()) {
                addonsLines.add(scanner.nextLine());
            }
        }

        try(FileEditor editor = new FileEditor(addonsFile)) {
            AtomicBoolean plugin = new AtomicBoolean(false);
            AtomicBoolean added = new AtomicBoolean(false);
            editor.forLines(line -> {
                if(line.getText().startsWith("[") && line.getText().endsWith("]")) {
                    plugin.set(line.getText().equals("[plugin]"));
                    return;
                }
                if(plugin.get() && line.getText().startsWith("Salty%20Chat")) {
                    if(!added.get()) {
                        line.setAddBefore(addonsLines);
                        added.set(true);
                    }
                    line.setAction(FileEditor.Line.ACTION_DELETE);
                }
            });

            editor.saveFile();
        }

        File pluginsFolder = new File(ts3ClientFolder, "plugins/");
        FileUtils.copyDirectoryToDirectory(new File(src, "SaltyChat/"), pluginsFolder);
        FileUtils.copyFileToDirectory(new File(src, "SaltyChat_win32.dll"), pluginsFolder);
        FileUtils.copyFileToDirectory(new File(src, "SaltyChat_win64.dll"), pluginsFolder);
    }

    private static void removeSaltyChat(File addonsFile, File ts3ClientFolder) throws IOException {
        try(FileEditor editor = new FileEditor(addonsFile)) {
            AtomicBoolean plugin = new AtomicBoolean(false);
            editor.forLines(line -> {
                if(line.getText().startsWith("[") && line.getText().endsWith("]")) {
                    plugin.set(line.getText().equals("[plugin]"));
                    return;
                }
                if(plugin.get() && line.getText().startsWith("Salty%20Chat")) {
                    line.setAction(FileEditor.Line.ACTION_DELETE);
                }
            });

            editor.saveFile();
        }

        File pluginsFolder = new File(ts3ClientFolder, "plugins/");
        FileUtils.deleteDirectory(new File(pluginsFolder, "SaltyChat/"));
        FileUtils.delete(new File(pluginsFolder, "SaltyChat_win32.dll"));
        FileUtils.delete(new File(pluginsFolder, "SaltyChat_win64.dll"));
    }

    private static void initCopySaltyChat(File addonsFile, File ts3ClientFolder, File dest) throws IOException {
        if(!dest.exists()) {
            if(!dest.mkdirs()) {
                message(dest.getAbsolutePath() + "\ncouldn't be created", "Close");
                return;
            }
        }

        List<String> iniContent = new ArrayList<>();

        try(FileEditor editor = new FileEditor(addonsFile)) {
            AtomicBoolean plugins = new AtomicBoolean(false);
            editor.forLines(line -> {
                if(line.getText().startsWith("[") && line.getText().endsWith("]")) {
                    plugins.set(line.getText().equals("[plugin]"));
                    return;
                }
                if(plugins.get()) {
                    if(line.getText().startsWith("Salty%20Chat")) {
                        iniContent.add(line.getText());
                    }
                }
            });
        }

        try(FileWriter writer = new FileWriter(new File(dest, "addons_content"))) {
            for(String s : iniContent) {
                writer.append(s).append("\n");
            }
        }

        FileUtils.copyDirectoryToDirectory(new File(ts3ClientFolder, "plugins/SaltyChat/"), dest);
        FileUtils.copyFileToDirectory(new File(ts3ClientFolder, "plugins/SaltyChat_win32.dll"), dest);
        FileUtils.copyFileToDirectory(new File(ts3ClientFolder, "plugins/SaltyChat_win64.dll"), dest);
    }

}

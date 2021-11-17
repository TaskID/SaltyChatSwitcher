package com.github.taskid.saltychatswitcher; // SaltyChatSwitcher, 10:37 02.11.2021

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class FileEditor implements Closeable {

    private final File file;

    private Scanner scanner = null;
    private FileWriter writer = null;

    private final List<String> content;

    public FileEditor(File file) throws IOException {
        this.file = file;
        this.content = new ArrayList<>();
        readFile();
    }

    public void readFile() throws FileNotFoundException {
        scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            content.add(scanner.nextLine());
        }
        scanner.close();
    }

    public void saveFile() throws IOException {
        writer = new FileWriter(file);
        for(String s : content) {
            writer.append(s).append("\n");
        }
        writer.close();
    }

    public void forLines(Consumer<Line> consumer) {
        Line[] lines = new Line[content.size()];

        for (int i = 0; i < content.size(); i++) {
            Line line = new Line(content, i);
            consumer.accept(line);
            lines[i] = line;
        }

        content.clear();
        for (Line line : lines) {
            if (line.getAddBefore() != null) {
                content.addAll(line.getAddBefore());
            }
            if (line.getAction() == Line.ACTION_OVERRIDE) {
                content.add(line.getText());
            } else if (line.getAction() == Line.ACTION_NOTHING) {
                content.add(line.getText());
            }
        }
    }

    public void printContent() {
        for(String s : content) {
            System.out.println(s);
        }
    }

    @Override
    public void close() throws IOException {
        if (scanner != null) scanner.close();
        if (writer != null) writer.close();
    }

    public static class Line {

        public static final int ACTION_NOTHING = 0;
        public static final int ACTION_DELETE = 1;
        public static final int ACTION_OVERRIDE = 2;


        private final int number;
        private String text;

        private int action = ACTION_NOTHING;

        private List<String> addBefore = null;

        public Line(List<String> list, int number) {
            this.number = number;
            this.text = list.get(number);
        }

        public int getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public int getAction() {
            return action;
        }

        public List<String> getAddBefore() {
            return addBefore;
        }

        public void setAddBefore(List<String> addBefore) {
            this.addBefore = addBefore;
        }
    }

}

package com.example.parallelsearchonstrings;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelSearchApp extends Application {

    private List<String> strings;
    private TextField searchTextField;

    private Label statusLabel;

    private ListView<String> resultListView;
    private static final String filePath = "C:/Users/trush/IdeaProjects/Parallel Search on Strings/src/main/java/com/example/parallelsearchonstrings/strings.csv";

    @Override
    public void init() throws Exception {
        super.init();
        createStrings();
    }

    @Override
    public void start(Stage stage) {
        strings = loadStringsFromCSVFile();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        searchTextField = new TextField();
        searchTextField.setPromptText("Enter the text to search");

        resultListView = new ListView<>();

        statusLabel = new Label("Execution Time: ");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> startSearch());

        VBox searchBox = new VBox(10, searchTextField, searchButton, statusLabel);
        root.setTop(searchBox);

        ScrollPane scrollPane = new ScrollPane(resultListView);
        root.setCenter(scrollPane);


        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.setTitle("Parallel Search Application");
        stage.show();

    }

    private void startSearch() {
        // Method to use all the available CPUs when number of CPUs not specified.
        startSearch(Runtime.getRuntime().availableProcessors());
    }

    private void startSearch(int numCPUs) {

        String searchText = searchTextField.getText().toLowerCase();

        Task<List<String>> searchTask = new Task<>() {
            @Override
            protected List<String> call() {
                List<String> foundStrings = new ArrayList<>();
                long startTime = System.currentTimeMillis();

                try (ExecutorService executor = Executors.newWorkStealingPool(numCPUs)) {

                    int availableProcessors = Runtime.getRuntime().availableProcessors();
                    int usedCPUs = Math.min(numCPUs, availableProcessors);

                    System.out.println("Used CPUs: " + usedCPUs);

                    List<Callable<Void>> callables = new ArrayList<>();
                    for (String str : strings) {
                        if (str.toLowerCase().contains(searchText)) {
                            callables.add(() -> {
                                Platform.runLater(() -> foundStrings.add(str));
                                return null;
                            });
                        }
                    }
                    executor.invokeAll(callables);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;

                Platform.runLater(() -> {
                    statusLabel.setText("Execution Time: " + executionTime + " ms");
                    resultListView.setItems(FXCollections.observableArrayList(foundStrings));
                });
                return foundStrings;
            }

        };
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void createStrings() {
        List<String> strings = new ArrayList<>();
        for (char c1 = 'A'; c1 <= 'Z'; c1++) {
            for (char c2 = 'A'; c2 <= 'Z'; c2++) {
                for (char c3 = 'A'; c3 <= 'Z'; c3++) {
                    for (char c4 = 'A'; c4 <= 'Z'; c4++) {
                        strings.add(String.valueOf(c1) + c2 + c3 + c4);
                    }
                }
            }
        }
        Collections.shuffle(strings);
        // System.out.println(strings);
        saveStringsToCSV(strings);
    }

    private void saveStringsToCSV(List<String> strings) {

        try {
            File file = new File(filePath);
            CSVWriter writer = new CSVWriter(new FileWriter(file, true));
            for (String str : strings) {
                writer.writeNext(new String[]{str});
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> loadStringsFromCSVFile() {
        List<String> loadedStrings = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new FileReader(filePath));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                Collections.addAll(loadedStrings, nextLine);
            }
            reader.close();

        } catch (IOException | CsvValidationException e) {
            System.out.println("Something went wrong while loading strings from csv file");
        }
        return loadedStrings;
    }

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Program terminated. Performing cleanup tasks...");
            File file = new File(filePath);
            try {
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println("File deleted successfully");
                    } else {
                        System.out.println("File was not deleted");
                    }
                } else {
                    System.out.println("File does not exists");
                }
            } catch (SecurityException e) {
                System.out.println("Insufficient permissions to delete the file: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An error occurred while deleting the file:" + e.getMessage());
            }
        }));
        launch();
    }
}
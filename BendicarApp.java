import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;

public class BendicarApp extends Application {

    private final TableView<Car> tableView = new TableView<>();
    private final ObservableList<Car> carList = FXCollections.observableArrayList();

    // Database Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bendi_car";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Header
        Label titleLabel = new Label("Bendicar Management System");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setAlignment(Pos.CENTER);

        // Kolom untuk tabel
        TableColumn<Car, String> plateColumn = new TableColumn<>("Plat Nomor");
        plateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlateNumber()));

        TableColumn<Car, String> modelColumn = new TableColumn<>("Model Mobil");
        modelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModel()));

        TableColumn<Car, String> colorColumn = new TableColumn<>("Warna");
        colorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getColor()));

        tableView.getColumns().addAll(plateColumn, modelColumn, colorColumn);
        tableView.setItems(carList);
        tableView.setPrefHeight(200);

        // Input field
        TextField plateInput = new TextField();
        plateInput.setPromptText("Plat Nomor");
        plateInput.setPrefWidth(150);

        TextField modelInput = new TextField();
        modelInput.setPromptText("Model Mobil");
        modelInput.setPrefWidth(150);

        TextField colorInput = new TextField();
        colorInput.setPromptText("Warna");
        colorInput.setPrefWidth(150);

        // Tombol
        Button addButton = new Button("Insert");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");

        // Event untuk tombol Insert
        addButton.setOnAction(e -> {
            String plate = plateInput.getText();
            String model = modelInput.getText();
            String color = colorInput.getText();

            if (!plate.isEmpty() && !model.isEmpty() && !color.isEmpty()) {
                if (insertCar(plate, model, color)) {
                    carList.add(new Car(plate, model, color));
                    plateInput.clear();
                    modelInput.clear();
                    colorInput.clear();
                }
            } else {
                showAlert("Input Error", "Semua field harus diisi!");
            }
        });

        // Event untuk tombol Delete
        deleteButton.setOnAction(e -> {
            Car selectedCar = tableView.getSelectionModel().getSelectedItem();
            if (selectedCar != null) {
                if (deleteCar(selectedCar.getPlateNumber())) {
                    carList.remove(selectedCar);
                }
            } else {
                showAlert("Selection Error", "Pilih data yang akan dihapus!");
            }
        });

        // Event untuk tombol Update
        updateButton.setOnAction(e -> {
            Car selectedCar = tableView.getSelectionModel().getSelectedItem();
            if (selectedCar != null) {
                String plate = plateInput.getText();
                String model = modelInput.getText();
                String color = colorInput.getText();

                if (!plate.isEmpty() && !model.isEmpty() && !color.isEmpty()) {
                    if (updateCar(selectedCar.getPlateNumber(), plate, model, color)) {
                        selectedCar.setPlateNumber(plate);
                        selectedCar.setModel(model);
                        selectedCar.setColor(color);
                        tableView.refresh();
                        plateInput.clear();
                        modelInput.clear();
                        colorInput.clear();
                    }
                } else {
                    showAlert("Input Error", "Semua field harus diisi untuk update!");
                }
            } else {
                showAlert("Selection Error", "Pilih data yang akan diperbarui!");
            }
        });

        // Layout untuk input dan tombol
        HBox inputBox = new HBox(10, plateInput, modelInput, colorInput);
        inputBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, titleLabel, tableView, inputBox, buttonBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Load data awal dari database
        loadCarsFromDatabase();

        // Scene dan Stage
        Scene scene = new Scene(root, 700, 400);
        primaryStage.setTitle("Bendicar Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Fungsi untuk mengambil data dari database
    private void loadCarsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mobil")) {

            while (rs.next()) {
                String plate = rs.getString("Plat Nomor");
                String model = rs.getString("Model");
                String color = rs.getString("Warna");
                carList.add(new Car(plate, model, color));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Gagal mengambil data dari database: " + e.getMessage());
        }
    }

    // Fungsi untuk menambah data ke database
    private boolean insertCar(String plate, String model, String color) {
        String query = "INSERT INTO mobil (`Plat Nomor`, `Model`, `Warna`) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, plate);
            pstmt.setString(2, model);
            pstmt.setString(3, color);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            showAlert("Database Error", "Gagal menambah data: " + e.getMessage());
            return false;
        }
    }

    // Fungsi untuk menghapus data dari database
    private boolean deleteCar(String plate) {
        String query = "DELETE FROM mobil WHERE `Plat Nomor` = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, plate);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            showAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
            return false;
        }
    }

    // Fungsi untuk memperbarui data di database
    private boolean updateCar(String oldPlate, String newPlate, String model, String color) {
        String query = "UPDATE mobil SET `Plat Nomor` = ?, `Model` = ?, `Warna` = ? WHERE `Plat Nomor` = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newPlate);
            pstmt.setString(2, model);
            pstmt.setString(3, color);
            pstmt.setString(4, oldPlate);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            showAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            return false;
        }
    }

    // Kelas untuk data mobil
    public static class Car {
        private String plateNumber;
        private String model;
        private String color;

        public Car(String plateNumber, String model, String color) {
            this.plateNumber = plateNumber;
            this.model = model;
            this.color = color;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    // Fungsi untuk menampilkan alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coursemanager;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelo.Alumno;
import modelo.LocalDateAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author Dani
 */
public class NewStudentWindowController implements Initializable {

    private ObservableList<Alumno> observableListAlumnos;
    
    @FXML
    private TextField dniText;
    @FXML
    private TextField nombreText;
    @FXML
    private TextField edadText;
    @FXML
    private TextField direccionText;
    @FXML
    private TextField fechaText;
    @FXML
    private Button atrasButton;
    @FXML
    private Button anadeButton;
    @FXML
    private ImageView imageView;

    private String photoPath = "";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // insertar la fecha actual
            LocalDateAdapter date = new LocalDateAdapter();
            fechaText.setText(date.marshal(LocalDate.now()));
        } catch (Exception ex) {
            Logger.getLogger(NewStudentWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @FXML
    private void handleImageViewClick(MouseEvent event){
        FileChooser fileChooser = new FileChooser();
        
        fileChooser.setTitle("Abrir fichero");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        
        File selectedFile = fileChooser.showOpenDialog(
                ((Node)event.getSource()).getScene().getWindow());
        
        if(selectedFile != null){
            photoPath = selectedFile.getAbsolutePath();
        }

        Image image = loadFoto();
        imageView.setImage(image);
    }

    @FXML
    private void handleImageViewDragOver(DragEvent event) {
        if (event.getGestureSource() != imageView
                && event.getDragboard().hasString() || event.getDragboard().hasUrl()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleImageViewDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        String urlStr = "";
        if (db.hasUrl()) {
            urlStr = db.getUrl();
        } else if (db.hasString()) {
            urlStr = db.getString();
        }
        
        if (!urlStr.isEmpty()) {
            try {
                URL url = new URL(urlStr);
                photoPath = Paths.get(url.toURI()).toString();
                Image image = loadFoto();
                imageView.setImage(image);
                success = true;
            } catch (MalformedURLException | URISyntaxException e) {
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Carga la foto a partir de la dirección del fichero
     * @return imagen
     */
    private Image loadFoto() {
        InputStream imgStream;

        Image noPhoto = new Image(getClass().getClassLoader().getResourceAsStream("images/no_photo.png"));

        try {
            imgStream = new FileInputStream(photoPath);
        } catch (FileNotFoundException e) {
            return noPhoto;
        }

        Image img = new Image(imgStream);
        if (img.isError()) {
            return noPhoto;
        }
        return img;
    }
    
    private LocalDate loadFecha() throws Exception {
        LocalDateAdapter date = new LocalDateAdapter();
        return date.unmarshal(fechaText.getText());
    }
    
    @FXML
    private void handleOnActionAtrasButton(ActionEvent event) {
        if (checkSomeField() && !Modal.showConfirmation("¿Seguramente quiere salir sin guardar los datos?")){
            return;
        }

        close();
    }
    /**
     * comprueba si ha sido añadido alguna informacion
     * @return false -> ninguna informacion añadita
     *          true -> alguno campo de incluye informacion
     */
    private boolean checkSomeField(){
        return !dniText.getText().isEmpty() ||
                !nombreText.getText().isEmpty() ||
                !edadText.getText().isEmpty() ||
                !direccionText.getText().isEmpty() ||
                !photoPath.isEmpty();
    }
            
    @FXML
    private void handleOnActionAnadeButton(ActionEvent event) throws Exception {
        Alumno newAlumno = new Alumno();

        int age;

        try {
            if (dniText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca el DNI!", dniText);
            }

            if (nombreText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca el nombre!", dniText);
            }

            if (edadText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca la edad!", edadText);
            }

            try {
                age = Integer.parseInt(edadText.getText());
            } catch (NumberFormatException e) {
                throw new ValidationException("¡La edad tiene que ser un número entero!", edadText);
            }

            if (age < 1) {
                throw new ValidationException("¡La edad tiene que ser positiva!", edadText);
            }

            if (direccionText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca la dirección!", direccionText);
            }

            if (observableListAlumnos.stream().anyMatch(student -> student.getDni().equals(dniText.getText()))) {
                throw new ValidationException("¡Ya existe un alumno con este DNI!", dniText);
            }
        } catch (ValidationException e) {
            e.handle();
            return;
        }

        if (photoPath.isEmpty() &&
                !Modal.showConfirmation("No ha seleccionado una fotografía.\n¿Desea continuar sin añadirla?")) {
            return;
        }

        newAlumno.setDni(dniText.getText());
        newAlumno.setNombre(nombreText.getText());
        newAlumno.setEdad(age);
        newAlumno.setDireccion(direccionText.getText());
        newAlumno.setFechadealta(loadFecha());
        newAlumno.setFoto(loadFoto());

        observableListAlumnos.add(newAlumno);

        close();
    }

    public void initWindow(ObservableList<Alumno> observableListAlumnos) {
        this.observableListAlumnos = observableListAlumnos;
    }

    private void close() {
        ((Stage)dniText.getScene().getWindow()).close();
    }
    
}

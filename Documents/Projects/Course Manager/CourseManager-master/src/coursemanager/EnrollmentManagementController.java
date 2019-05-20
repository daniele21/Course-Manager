/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coursemanager;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modelo.Alumno;
import modelo.Curso;
import modelo.Matricula;
import modelo.Dias;

/**
 * FXML Controller class
 *
 * @author Dani
 */
public class EnrollmentManagementController implements Initializable {
    
    private ObservableList<Matricula> allEnrollments;
    private ObservableList<Alumno> notEnrolled;

    private FilteredList<Matricula> filteredEnrolled;
    private FilteredList<Alumno> filteredNotEnrolled;

    private BooleanBinding enrollmentsLimit;
    
    private Curso course;

    @FXML
    private TextField filterText;
    @FXML
    private TableView<Matricula> enrolledTable;
    @FXML
    private TableView<Alumno> notEnrolledTable;
    @FXML
    private TableColumn<Matricula, String> enrolledNameColumn, enrolledDateColumn;
    @FXML
    private TableColumn<Alumno, String> notEnrolledNameColumn;
    @FXML
    private Button enrollButton, unenrollButton;

    /**
     * Initiif (courseList.stream().anyMatch(course -> course.getTitulodelcurso().equals(titleText.getText()))) {
     throw new ValidationException("¡Ya existe un curso con este título!", titleText);
     }alizes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        enrolledNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAlumno().getNombre()));
        enrolledDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha().toString()));
        notEnrolledNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));

        enrollButton.disableProperty().bind(Bindings.equal(-1, notEnrolledTable.getSelectionModel().selectedIndexProperty()));
        unenrollButton.disableProperty().bind(Bindings.equal(-1, enrolledTable.getSelectionModel().selectedIndexProperty()));

        filterText.textProperty().addListener((obj, oldVal, newVal) -> {
            if (filteredEnrolled != null) {
                filteredEnrolled.setPredicate(enr -> enr.getAlumno().getNombre().toLowerCase().contains(newVal));
            }
            if (filteredNotEnrolled != null) {
                filteredNotEnrolled.setPredicate(s -> s.getNombre().toLowerCase().contains(newVal));
            }
        });
    }
    
    public void initWindow(Curso course, ObservableList<Alumno> allStudents, ObservableList<Matricula> allEnrollments) {
        this.allEnrollments = allEnrollments;
        this.course = course;

        ObservableList<Matricula> enrolled = allEnrollments.filtered(enr -> enr.getCurso().getTitulodelcurso().equals(course.getTitulodelcurso()));
        enrollmentsLimit = Bindings.lessThan(Bindings.size(enrolled), course.getNumeroMaximodeAlumnos());

        this.notEnrolled = FXCollections.observableArrayList(allStudents);
        for (Matricula enr: enrolled) {
            notEnrolled.removeIf(s -> s.getDni().equals(enr.getAlumno().getDni()));
        }

        filteredEnrolled = new FilteredList<>(enrolled);
        filteredNotEnrolled = new FilteredList<>(notEnrolled);

        enrolledTable.setItems(filteredEnrolled);
        notEnrolledTable.setItems(filteredNotEnrolled);
    }
    @FXML
    private void handleEnrollAction() {
        Alumno student = notEnrolledTable.getSelectionModel().getSelectedItem();
        if (student == null) {
            return;
        }

        if (!enrollmentsLimit.get()) {
            Modal.showWarning("¡No se puede superar el número máximo\nde alumnos matriculados en el curso!");
            return;
        }

        Optional<Curso> overlap = findOverlapping(student);
        if(overlap.isPresent()) {
            Modal.showWarning("¡Esta asignatura se solapa\ncon " + overlap.get().getTitulodelcurso() + "!");
            return;
        }
        
        allEnrollments.add(new Matricula(LocalDate.now(), course, student));
        notEnrolled.remove(student);
        
    }

    private Optional<Curso> findOverlapping(Alumno student) {
        return allEnrollments.stream()
                .filter(enr -> enr.getAlumno().getDni().equals(student.getDni()))
                .map(Matricula::getCurso)
                .filter(c -> (c.getFechainicio().isBefore(course.getFechafin()) && course.getFechainicio().isBefore((c.getFechafin()))))
                .filter(c -> c.getHora().equals(course.getHora()))
                .filter(c -> {
                    Set<Dias> cDays = new HashSet<>(c.getDiasimparte());
                    cDays.retainAll(course.getDiasimparte());
                    return !cDays.isEmpty();
                })
                .findAny();
    }



    @FXML
    private void handleUnenrollAction() {
        Matricula enrollment = enrolledTable.getSelectionModel().getSelectedItem();
        if (enrollment == null) {
            return;
        }
        allEnrollments.remove(enrollment);
        notEnrolled.add(enrollment.getAlumno());
    }

    @FXML
    private void handleOkAction() {
        close();
    }

    private void close() {
        ((Stage)filterText.getScene().getWindow()).close();
    }
}

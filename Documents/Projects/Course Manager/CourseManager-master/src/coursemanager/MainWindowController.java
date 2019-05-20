/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coursemanager;

import accesoaBD.AccesoaBD;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import modelo.Alumno;
import modelo.Curso;
import modelo.Matricula;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Dani
 */

public class MainWindowController implements Initializable {

    private AccesoaBD db;
    
    private Stage stage;
    
    private ObservableList<Alumno> observableListAlumnos;
    private ObservableList<Curso> observableListCursos;
    private ObservableList<Matricula> observableListMatriculas;

    private BooleanProperty dirty;

    @FXML
    private Button saveButton;
    @FXML
    private ListView<Alumno> listAlumno;
    @FXML
    private Button anadeAlumnoButton;
    @FXML
    private Label noStudentSelectedLabel;
    @FXML
    private HBox studentDetails;
    @FXML
    private Label studentDniLabel, studentNameLabel, studentAgeLabel, studentAddressLabel, studentRegistrationDateLabel;
    @FXML
    private Button manageEnrollmentsButton, removeCourseButton;
    @FXML
    private Button removeStudentButton;
    @FXML
    private ImageView imageView;
    @FXML
    private Label courseTitleLabel, courseProfessorLabel, courseNumEnrollmentsLabel, courseStartDateLabel, courseEndDateLabel, courseTimeLabel, courseWeekdaysLabel, courseClassroomLabel;
    @FXML
    private Label noCourseSelectedLabel;
    @FXML
    private GridPane courseDetails;
    @FXML
    private ListView<Curso> listCurso;
    @FXML
    private Button anadeCursoButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        db = new AccesoaBD();
        
        observableListAlumnos = FXCollections.observableList(db.getAlumnos());
        observableListCursos = FXCollections.observableList(db.getCursos());
        observableListMatriculas = FXCollections.observableList(db.getMatriculas());

        dirty = new SimpleBooleanProperty();
        saveButton.disableProperty().bind(dirty.not());

        observableListAlumnos.addListener((ListChangeListener<Alumno>) (c -> dirty.set(true)));
        observableListCursos.addListener((ListChangeListener<Curso>) (c -> dirty.set(true)));
        observableListMatriculas.addListener((ListChangeListener<Matricula>) (c -> dirty.set(true)));

        listAlumno.setItems(observableListAlumnos);
        listCurso.setItems(observableListCursos);
        
        listAlumno.setCellFactory(c -> new AlumnoListCell());
        listCurso.setCellFactory(c -> new CursoListCell());

        BooleanBinding noStudentSelected = Bindings.equal(-1, listAlumno.getSelectionModel().selectedIndexProperty());
        studentDetails.visibleProperty().bind(noStudentSelected.not());
        noStudentSelectedLabel.visibleProperty().bind(noStudentSelected);
        removeStudentButton.disableProperty().bind(noStudentSelected);

        BooleanBinding noCourseSelected = Bindings.equal(-1, listCurso.getSelectionModel().selectedIndexProperty());
        courseDetails.visibleProperty().bind(noCourseSelected.not());
        noCourseSelectedLabel.visibleProperty().bind(noCourseSelected);
        manageEnrollmentsButton.disableProperty().bind(noCourseSelected);
        removeCourseButton.disableProperty().bind(noCourseSelected);

        listAlumno.getSelectionModel().selectedIndexProperty().addListener((obj, oldVal, newVal) -> updateStudentFields(newVal.intValue()));
        listCurso.getSelectionModel().selectedIndexProperty().addListener((obj, oldVal, newVal) -> updateCourseFields(newVal.intValue()));
    }

    public void initWindow(Stage stage){
        this.stage = stage;

        stage.setOnCloseRequest(event -> {
            if (dirty.get()) {
                ButtonType response = Modal.showThreeWayConfirmation("¿Desea guardar los cambios antes de salir?");
                if (response == ButtonType.YES) {
                    db.salvar();
                } else if (response == ButtonType.CANCEL) {
                    event.consume();
                }
            }
        });
    }

    /**
     * Actualiza los datos de un alumno selecionado en la lista
     */
    private void updateStudentFields(int index) {
        if(index == -1) {
            return;
        }
        Alumno student = observableListAlumnos.get(index);
        if (student == null) {
            return;
        }

        studentDniLabel.setText(student.getDni());
        studentNameLabel.setText(student.getNombre());
        studentAgeLabel.setText(String.valueOf(student.getEdad()) + " años");
        studentAddressLabel.setText(student.getDireccion());
        studentRegistrationDateLabel.setText(student.getFechadealta().toString());
        imageView.setImage(student.getFoto());
    }

    private void updateCourseFields(int index) {
        if (index == -1) {
            return;
        }
        Curso course = observableListCursos.get(index);
        if (course == null) {
            return;
        }

        int numEnrollments = db.getMatriculasDeCurso(course).size();
        int maxEnrollments = course.getNumeroMaximodeAlumnos();

        courseTitleLabel.setText(course.getTitulodelcurso());
        courseProfessorLabel.setText(course.getProfesorAsignado());
        courseNumEnrollmentsLabel.setText(numEnrollments + "/" + maxEnrollments);
        courseStartDateLabel.setText(course.getFechainicio().toString());
        courseEndDateLabel.setText(course.getFechafin().toString());
        courseTimeLabel.setText(course.getHora().toString());
        courseClassroomLabel.setText(course.getAula());

        char[] weekdaysFull = {'L', 'M', 'X', 'J', 'V', 'S', 'D'};
        char[] weekdays = {'-', '-', '-', '-', '-', '-', '-'};
        for (modelo.Dias day: course.getDiasimparte()) {
            int i = day.ordinal();
            weekdays[i] = weekdaysFull[i];
        }
        courseWeekdaysLabel.setText(String.valueOf(weekdays));
    }

    @FXML
    private void handleOnActionAnadeAlumnoButton(ActionEvent event) {
        Modal.<NewStudentWindowController>showFXML(
                getClass().getResource("NewStudentWindow.fxml"),
                ctrl -> ctrl.initWindow(observableListAlumnos),
                "Añadir alumno",
                stage
        );
    }

    @FXML
    private void handleOnActionAddCourseButton(ActionEvent event) {
        Modal.<NewCourseWindowController>showFXML(
                getClass().getResource("NewCourseWindow.fxml"),
                ctrl -> ctrl.initWindow(observableListCursos),
                "Añadir curso",
                stage
        );
    }

    @FXML
    private void handleManageEnrollmentsAction(ActionEvent event) {
        Curso course = listCurso.getSelectionModel().getSelectedItem();
        if (course == null) {
            return;
        }

        Modal.<EnrollmentManagementController>showFXML(
                getClass().getResource("EnrollmentManagement.fxml"),
                ctrl -> ctrl.initWindow(course, observableListAlumnos, observableListMatriculas),
                "Matrículas en "+course.getTitulodelcurso(),
                stage
        );

        updateCourseFields(listCurso.getSelectionModel().getSelectedIndex());
    }
    
    
    @FXML
    private void handleOnActionBorraButton(ActionEvent event) {
        int index = listAlumno.getSelectionModel().getSelectedIndex();
        
        if(index == -1) {
            return;
        }
        Alumno student = listAlumno.getSelectionModel().getSelectedItem();
        if (student == null) {
            return;
        }

        if (observableListMatriculas.stream().anyMatch(enr -> enr.getAlumno().getDni().equals(student.getDni()))) {
            Modal.showWarning("¡No se puede borrar al alumno mientras\nesté matriculado en asignaturas!");
            return;
        }

        if (Modal.showConfirmation("¿Seguramente quiere borrar al alumno?")) {
            observableListAlumnos.remove(student);
        }
    }

    @FXML
    private void handleRemoveCourseAction(ActionEvent event) {
        int index = listCurso.getSelectionModel().getSelectedIndex();

        if(index == -1) {
            return;
        }
        Curso course = listCurso.getSelectionModel().getSelectedItem();
        if (course == null) {
            return;
        }

        if (observableListMatriculas.stream().anyMatch(enr -> enr.getCurso().getTitulodelcurso().equals(course.getTitulodelcurso()))) {
            Modal.showWarning("¡No se puede borrar el curso mientras\nhayan alumnos matriculados!");
            return;
        }

        if (Modal.showConfirmation("¿Seguramente quiere borrar el curso?")) {
            observableListCursos.remove(course);
        }
    }

    @FXML
    private void handleSaveAction(ActionEvent event) {
        try {
            db.salvar();
            dirty.set(false);
        } catch (Exception e) {
            e.printStackTrace();
            Modal.showError("¡No se ha podido guardar los cambios!");
        }
    }
    
    class AlumnoListCell extends ListCell<Alumno> {
        @Override
        protected void updateItem(Alumno item, boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty)
                setText(null);
            else
                setText(item.getNombre() + ", " + item.getEdad() + " años");
            
        }
    }
    class CursoListCell extends ListCell<Curso> {
        @Override
        protected void updateItem(Curso item, boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty)
                setText(null);
            else
                setText(item.getTitulodelcurso());
        }
    }
}

package coursemanager;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import modelo.Curso;
import modelo.Dias;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

public class NewCourseWindowController {
    @FXML
    private TextField titleText, professorText, maxStudentsText, classroomText;
    @FXML
    private Spinner<Integer> hourSpinner, minuteSpinner;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private ToggleButton dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;

    private Map<ToggleButton, Dias> weekdayToggles;

    private IntegerSpinnerValueFactory  hourFactory, minuteFactory;

    private ObservableList<Curso> courseList;

    public void initialize() {

        hourFactory = new IntegerSpinnerValueFactory(0, 23, 9);
        hourSpinner.setValueFactory(hourFactory);
        minuteFactory = new IntegerSpinnerValueFactory(0, 59, 0, 5);
        minuteFactory.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value <= 9 ? "0" + value.toString() : value.toString();
            }

            @Override
            public Integer fromString(String string) {
                return Integer.valueOf(string, 10);
            }
        });
        minuteSpinner.setValueFactory(minuteFactory);

        weekdayToggles = new HashMap<>();
        weekdayToggles.put(dayMon, Dias.Lunes);
        weekdayToggles.put(dayTue, Dias.Martes);
        weekdayToggles.put(dayWed, Dias.Miercoles);
        weekdayToggles.put(dayThu, Dias.Jueves);
        weekdayToggles.put(dayFri, Dias.Viernes);
        weekdayToggles.put(daySat, Dias.Sabado);
        weekdayToggles.put(daySun, Dias.Domingo);
    }

    public void initWindow(ObservableList<Curso> courseList) {
        this.courseList = courseList;
    }

    public void handleBackAction(ActionEvent actionEvent) {
        close();
    }

    public void handleAddCourseAction(ActionEvent actionEvent) {
        ArrayList<Dias> weekdays = new ArrayList<>();

        for (ToggleButton day: weekdayToggles.keySet()) {
            if (day.isSelected()) {
                weekdays.add(weekdayToggles.get(day));
            }
        }

        int maxStudents;

        try {
            if (titleText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca el título del curso!", titleText);
            }

            if (professorText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca al profesor asignado!", professorText);
            }

            try {
                maxStudents = Integer.parseInt(maxStudentsText.getText());
            } catch (NumberFormatException e) {
                throw new ValidationException("¡El número máximo de alumnos tiene que ser un número entero!", maxStudentsText);
            }
            if (maxStudents < 1) {
                throw new ValidationException("¡El número máximo de alumnos tiene que ser al menos 1!", maxStudentsText);
            }

            if (startDate.getValue() == null) {
                throw new ValidationException("¡Introduzca la fecha de inicio!", startDate);
            }

            if (endDate.getValue() == null) {
                throw new ValidationException("¡Introduzca la fecha de fin!", endDate);
            }

            if (startDate.getValue().isAfter(endDate.getValue())) {
                throw new ValidationException("¡La fecha de inicio tiene que ser anterior a la de fin!", startDate);
            }

            if (weekdays.isEmpty()) {
                throw new ValidationException("¡Seleccione al menos un día de semana!", dayMon);
            }

            if (classroomText.getText().isEmpty()) {
                throw new ValidationException("¡Introduzca el aula!", classroomText);
            }

            if (courseList.stream().anyMatch(course -> course.getTitulodelcurso().equals(titleText.getText()))) {
                throw new ValidationException("¡Ya existe un curso con este título!", titleText);
            }
        } catch (ValidationException e) {
            e.handle();
            return;
        }

        Curso course = new Curso();
        course.setTitulodelcurso(titleText.getText());
        course.setProfesorAsignado(professorText.getText());
        course.setNumeroMaximodeAlumnos(maxStudents);
        course.setFechainicio(startDate.getValue());
        course.setFechafin(endDate.getValue());
        course.setHora(LocalTime.of(hourFactory.getValue(), minuteFactory.getValue()));
        course.setAula(classroomText.getText());
        course.setDiasimparte(weekdays);

        courseList.add(course);
        close();
    }

    private void close() {
        ((Stage)titleText.getScene().getWindow()).close();
    }
}

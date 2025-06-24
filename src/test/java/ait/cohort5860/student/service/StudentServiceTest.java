package ait.cohort5860.student.service;

import ait.cohort5860.configuration.ServiceConfiguration;
import ait.cohort5860.student.dao.StudentRepository;
import ait.cohort5860.student.dto.ScoreDto;
import ait.cohort5860.student.dto.StudentCredentialsDto;
import ait.cohort5860.student.dto.StudentDto;
import ait.cohort5860.student.dto.StudentUpdateDto;
import ait.cohort5860.student.dto.exceptions.NotFoundException;
import ait.cohort5860.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes ={ServiceConfiguration.class})
@SpringBootTest
public class StudentServiceTest {
    private final long studentId = 1000;
    private final String name = "John";
    private final String password = "1234";
    private Student student;
    private final String examName = "Math";
    private final int score = 90;



    @Autowired
    private ModelMapper modelMapper;


    @MockitoBean
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    public void setUp() {
        student = new Student(studentId, "John","test");
        studentService = new StudentServiceImpl(studentRepository, modelMapper);
    }
    @Test
    void testAddStudentWhenStudentNotExist() {
        // Arrange
        StudentCredentialsDto dto = new StudentCredentialsDto(studentId,name,password);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // Act
        boolean result = studentService.addStudent(dto);

        //Assert
        assertTrue(result);
    }

    @Test
    void testAddStudentWhenStudentExist() {
        // Arrange
        StudentCredentialsDto dto = new StudentCredentialsDto(studentId,name,password);
        when(studentRepository.existsById(dto.getId())).thenReturn(true);

        // Act
        boolean result = studentService.addStudent(dto);

        //Assert
        assertFalse(result);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testFindStudentWhenStudentExists(){
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));

        // Act
        StudentDto studentDto = studentService.findStudent(studentId);
        //Assert
        assertNotNull(studentDto);
        assertEquals(studentId,studentDto.getId());

    }

    @Test
    void testFindStudentWhenStudentNotExists(){
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
       assertThrows(NotFoundException.class, () -> studentService.findStudent(studentId));

    }

    @Test
    void testRemoveStudent() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));
        // Act
        StudentDto studentDto = studentService.removeStudent(studentId);
        //Assert
        assertNotNull(studentDto);
        assertEquals(studentId,studentDto.getId());
        verify(studentRepository, times(1)).deleteById(studentId);

    }

    @Test
    void testUpdateStudent() {
        // Arrange
        String newName = "newName";
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));
        StudentUpdateDto dto = new StudentUpdateDto(newName, null);
        // Act
        StudentCredentialsDto studentCredentialsDto = studentService.updateStudent(studentId,dto);
        //Assert
        assertNotNull(studentCredentialsDto);
        assertEquals(studentId,studentCredentialsDto.getId());
        assertEquals("NewName", studentCredentialsDto.getName());
        assertEquals("password", studentCredentialsDto.getPassword());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testAddScore(){
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        ScoreDto scoreDto = new ScoreDto(examName,score);
        // Act
        boolean res = studentService.addScore(studentId,scoreDto);
        //Assert
        assertTrue(res);
        verify(studentRepository, times(1)).save(student);
    }
    @Test
    void testFindStudentsByName() {
        // Arrange
        Student student = new Student(studentId,name,password);
        when(studentRepository.findByNameIgnoreCase(name)). thenReturn(Stream.of(student));
        // Act
        List<StudentDto> res = studentService.findStudentsByName(name);
        //Assert
        assertNotNull(res);
        assertEquals(1,res.size());
        assertEquals(studentId,res.get(0).getId());
        verify(studentRepository).findByNameIgnoreCase(name);

    }

    @Test
    void testCountStudentsByName() {
        // Arrange
        Set<String> names = Set.of(name);
        when(studentRepository.countStudentsByNameIn(names)).thenReturn(1L);
        // Act
        Long count = studentService.countStudentsByName(names);
        //Assert
        assertEquals(1,count);
    }

    @Test
    void testFindStudentByExamMinScore() {
        // Arrange
        student.addScore(examName,score);
        when(studentRepository.findByExamAndScoresGreaterThan(examName, score - 1))
                .thenReturn(Stream.of(student));
        // Act
        List<StudentDto> res = studentService.findStudentsByExamNameMinScore(examName, score - 1);
        //Assert
        assertEquals(1,res.size());
        assertEquals(score,res.get(0).getScores().get(examName));
    }

}

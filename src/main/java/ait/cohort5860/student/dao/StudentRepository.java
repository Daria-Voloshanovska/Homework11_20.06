package ait.cohort5860.student.dao;

import ait.cohort5860.student.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface StudentRepository extends MongoRepository<Student,Long> {
//    Student save(Student student);
//
//    Optional<Student> findById(Long id);
//
//
//    void deleteById(Long id);
//
//    List<Student> findAll();

Stream<Student> findByNameIgnoreCase(String name);

@Query("{'scores.Math' : {'$gt' : 90}}")
    Stream<Student> findByExamAndScoresGreaterThan(String examName, Integer score);

@Query("{'name': {$in: ?0}}")
    long countStudentsByNameIn(Set<String> names);


}

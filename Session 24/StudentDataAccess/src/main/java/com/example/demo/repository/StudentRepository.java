package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Custom Query Method - By Department
    List<Student> findByDepartment(String department);

    // Custom Query Method - By Age
    List<Student> findByAge(int age);
}
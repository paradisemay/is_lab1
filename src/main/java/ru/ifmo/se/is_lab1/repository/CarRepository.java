package ru.ifmo.se.is_lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ifmo.se.is_lab1.domain.Car;

public interface CarRepository extends JpaRepository<Car, Long> {
}

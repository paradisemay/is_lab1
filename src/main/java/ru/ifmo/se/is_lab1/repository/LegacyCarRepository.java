package ru.ifmo.se.is_lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.ifmo.se.is_lab1.model.Car;

public interface LegacyCarRepository extends JpaRepository<Car, Long> {
}

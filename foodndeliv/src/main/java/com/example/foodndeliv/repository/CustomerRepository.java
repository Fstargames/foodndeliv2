package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "customers")
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}

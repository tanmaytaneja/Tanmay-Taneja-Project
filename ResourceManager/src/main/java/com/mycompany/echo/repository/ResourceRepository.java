package com.mycompany.echo.repository;

import com.mycompany.echo.model.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceRepository extends MongoRepository<Resource, String>{
    Resource findByResource(String resource);
}

package com.mycompany.echo.service;

import com.mycompany.echo.model.Resource;
import com.mycompany.echo.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public void saveResource(Resource resource) {
        resourceRepository.save(resource);
    }

    public Resource getResourceByName(String resource) {
        Optional<Resource> r = resourceRepository.findById(resource);
        return r.orElse(null);
    }

    public List<Resource> getAll(){
        return resourceRepository.findAll();
    }

    public void deleteByName(String resource){
        resourceRepository.deleteById(resource);
    }

    public boolean checkResourceExists(String resource) {
        return resourceRepository.existsById(resource);
    }
}

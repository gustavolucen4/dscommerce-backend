package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.CategoryDTO;
import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseExceptions;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Product product = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExceptions("Recurso não encontrado!!"));
        return new ProductDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductMinDTO> findAll(String name, Pageable pageable){
        Page<Product> productPage = repository.searchByName(name ,pageable);
        return productPage.map(ProductMinDTO::new);
    }

    @Transactional
    public ProductDTO insert(ProductDTO productDTO){

        Product product = new Product();
        copyDtoToEntity(product, productDTO);
        product = repository.save(product);

        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO productDTO){

        try{
            Product product = repository.getReferenceById(id);
            copyDtoToEntity(product, productDTO);
            product = repository.save(product);
            return new ProductDTO(product);
        }catch (EntityNotFoundException ex){
            throw new ResourceNotFoundExceptions("Recurso não encontrado!!");
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id){
        try {
            repository.deleteById(id);
        }catch (EmptyResultDataAccessException ex){
            throw new ResourceNotFoundExceptions("Recurso não encontrado!!");
        }catch (DataIntegrityViolationException ex){
            throw new DatabaseExceptions("Falha de integridade referencial.");
        }
    }

    private void copyDtoToEntity(Product product, ProductDTO productDTO){
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImgUrl(productDTO.getImgUrl());

        product.getCategories().clear();
        for (CategoryDTO dto : productDTO.getCategories()){
            Category category = new Category();
            category.setId(dto.getId());
            product.getCategories().add(category);
        }
    }
}

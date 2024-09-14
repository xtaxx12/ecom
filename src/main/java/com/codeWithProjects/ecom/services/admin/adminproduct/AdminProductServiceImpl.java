package com.codeWithProjects.ecom.services.admin.adminproduct;


import com.codeWithProjects.ecom.dto.ProductDto;
import com.codeWithProjects.ecom.entity.Category;
import com.codeWithProjects.ecom.entity.Product;
import com.codeWithProjects.ecom.repository.CategoryRepository;
import com.codeWithProjects.ecom.repository.ProductRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService{

    private final ProductRespository productRespository;

    private final CategoryRepository categoryRepository;


    public ProductDto addProduct(ProductDto productDto) throws IOException {

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImg(productDto.getImg().getBytes());  // Guardar la imagen como byte[]

        Category category = categoryRepository.findById(productDto.getCategoryId()).orElseThrow();
        product.setCategory(category);

        return productRespository.save(product).getDto();
    }

    public List<ProductDto> getAllProducts() {

        List<Product> products = productRespository.findAll();

        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    public List<ProductDto> getAllProductByName(String name) {

        List<Product> products = productRespository.findAllByNameContaining(name);

        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    public boolean deleteProductById(Long id) {
        Optional<Product> optionalProduct = productRespository.findById(id);
        if (optionalProduct.isPresent()) {
            productRespository.deleteById(id);
            return true;
        }
        return false;
    }

    public ProductDto getProductById(Long id) {
        Optional<Product> optionalProduct = productRespository.findById(id);

        if(optionalProduct.isPresent()) {

            return optionalProduct.get().getDto();
        }else{
            return null;
        }
    }

    public ProductDto updateProduct(Long productId ,ProductDto productDto) throws IOException {
        Optional<Product> optionalProduct = productRespository.findById(productId);
        Optional<Category> optionalCategory = categoryRepository.findById(productDto.getCategoryId());

        if(optionalProduct.isPresent() && optionalCategory.isPresent()) {
            Product product = optionalProduct.get();

            product.setName(productDto.getName());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setCategory(optionalCategory.get());


            if(productDto.getImg() != null) {
                product.setImg(productDto.getImg().getBytes());
            }
            return productRespository.save(product).getDto();


        }else{
            return null;
        }


    }
}
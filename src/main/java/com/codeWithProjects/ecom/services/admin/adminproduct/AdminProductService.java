package com.codeWithProjects.ecom.services.admin.adminproduct;

import com.codeWithProjects.ecom.dto.ProductDto;

import java.io.IOException;
import java.util.List;

public interface AdminProductService {

    ProductDto addProduct(ProductDto productDto) throws IOException;

    List<ProductDto> getAllProducts() ;

    List<ProductDto> getAllProductByName(String name) ;

    boolean deleteProductById(Long id);

    ProductDto getProductById(Long id);

    ProductDto updateProduct(Long productId ,ProductDto productDto) throws IOException;


}

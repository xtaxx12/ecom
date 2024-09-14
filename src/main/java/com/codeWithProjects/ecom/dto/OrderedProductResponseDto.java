package com.codeWithProjects.ecom.dto;


import com.codeWithProjects.ecom.entity.Product;
import lombok.Data;

import java.util.List;

@Data
public class OrderedProductResponseDto {

    private List<ProductDto> productDtoList;

    private Long productId;

    private Long orderAmount;
}



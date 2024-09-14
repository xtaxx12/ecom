package com.codeWithProjects.ecom.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.Lob;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductDto {

    private Long id;

    private String name;

    private Long price;

    private String description;

    private Long categoryId;

    private String categoryName;

    // Este campo no debe ser serializado directamente
    @JsonIgnore
    private MultipartFile img;

    // Este campo representa la imagen como un array de bytes
    private byte[] byteImg;

    private Long quantity;

   
}


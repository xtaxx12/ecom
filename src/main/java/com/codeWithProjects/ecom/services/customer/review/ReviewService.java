package com.codeWithProjects.ecom.services.customer.review;

import com.codeWithProjects.ecom.dto.OrderedProductResponseDto;
import com.codeWithProjects.ecom.dto.ReviewDto;

import java.io.IOException;

public interface ReviewService {


    public OrderedProductResponseDto getOrderedProductDetailsByOrderId(long orderId);

    ReviewDto giveReview(ReviewDto reviewDto) throws IOException;
}

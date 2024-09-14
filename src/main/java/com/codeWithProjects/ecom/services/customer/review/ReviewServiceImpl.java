package com.codeWithProjects.ecom.services.customer.review;


import com.codeWithProjects.ecom.dto.OrderedProductResponseDto;
import com.codeWithProjects.ecom.dto.ProductDto;
import com.codeWithProjects.ecom.dto.ReviewDto;
import com.codeWithProjects.ecom.entity.*;
import com.codeWithProjects.ecom.repository.OrderRepository;
import com.codeWithProjects.ecom.repository.ProductRespository;
import com.codeWithProjects.ecom.repository.ReviewRepository;
import com.codeWithProjects.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {


    private final OrderRepository orderRepository;
    private final ProductRespository productRespository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;


    public OrderedProductResponseDto getOrderedProductDetailsByOrderId(long orderId) {

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        OrderedProductResponseDto orderedProductResponseDto = new OrderedProductResponseDto();
        if(optionalOrder.isPresent()) {
            orderedProductResponseDto.setOrderAmount(optionalOrder.get().getAmount());

        List<ProductDto> productDtoList = new ArrayList<>();
            for(CartItems cartItems: optionalOrder.get().getCartItems()) {
                ProductDto productDto = new ProductDto();

                productDto.setId(cartItems.getProduct().getId());
                productDto.setName(cartItems.getProduct().getName());
                productDto.setPrice(cartItems.getProduct().getPrice());
                productDto.setQuantity(cartItems.getQuantity());

                productDto.setByteImg(cartItems.getProduct().getImg());

                productDtoList.add(productDto);

            }
            orderedProductResponseDto.setProductDtoList(productDtoList);
        }
        return orderedProductResponseDto;

    }

    public ReviewDto giveReview(ReviewDto reviewDto) throws IOException {
        Optional<Product> optionalProduct = productRespository.findById(reviewDto.getProductId());
        Optional<User> optionalUser = userRepository.findById(reviewDto.getUserId());


        if(optionalProduct.isPresent() && optionalUser.isPresent()) {
            Review review = new Review();

            review.setRating(reviewDto.getRating());
            review.setDescription(reviewDto.getDescription());
            review.setUser(optionalUser.get());
            review.setProduct(optionalProduct.get());
            review.setImg(reviewDto.getImg().getBytes());

            return reviewRepository.save(review).getDto();

        }

        return null;
    }
}

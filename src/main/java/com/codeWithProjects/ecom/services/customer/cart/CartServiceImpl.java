package com.codeWithProjects.ecom.services.customer.cart;



import com.codeWithProjects.ecom.dto.AddProductInCartDto;
import com.codeWithProjects.ecom.dto.CartItemsDto;
import com.codeWithProjects.ecom.dto.OrderDto;
import com.codeWithProjects.ecom.dto.PlaceOrderDto;
import com.codeWithProjects.ecom.entity.*;
import com.codeWithProjects.ecom.enums.OrderStatus;
import com.codeWithProjects.ecom.exceptions.ValidationException;
import com.codeWithProjects.ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemsRepository cartsItemsRepository;

    @Autowired
    private ProductRespository productRespository;

    @Autowired
    private CouponRepository couponRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    public ResponseEntity<?> addProductToCart(AddProductInCartDto addProductInCartDto) {

        // Buscar una orden activa para el usuario
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        if (activeOrder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("No active order found for user"));
        }

        // Verificar si el producto ya está en el carrito
        Optional<CartItems> optionalCartItems = cartsItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if (optionalCartItems.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse("Product already in cart"));
        }

        // Buscar el producto y el usuario
        Optional<Product> optionalProduct = productRespository.findById(addProductInCartDto.getProductId());
        Optional<User> optionalUser = userRepository.findById(addProductInCartDto.getUserId());

        // Validar la existencia del producto y del usuario
        if (optionalProduct.isPresent() && optionalUser.isPresent()) {
            Product product = optionalProduct.get();
            User user = optionalUser.get();

            // Crear un nuevo item de carrito
            CartItems cart = new CartItems();
            cart.setProduct(product);
            cart.setPrice(product.getPrice());
            cart.setQuantity(1L);
            cart.setUser(user);
            cart.setOrder(activeOrder);

            // Actualizar el carrito y la orden
            CartItems updatedCart = cartsItemsRepository.save(cart);
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() + cart.getPrice());
            activeOrder.setAmount(activeOrder.getAmount() + cart.getPrice());
            activeOrder.getCartItems().add(cart);
            orderRepository.save(activeOrder);

            // Responder con el item de carrito creado
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("User or Product not found"));
        }
    }
    // Método para crear una respuesta de error uniforme
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

    public OrderDto getCartByUserId(Long userId) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);

        // Si no se encuentra un pedido activo, puedes devolver null o un objeto OrderDto vacío según tu caso
        if (activeOrder == null) {
            return null; // O new OrderDto() vacío
        }

        List<CartItemsDto> cartItemsDtoList = activeOrder.getCartItems().stream()
                .map(CartItems::getCartDto) // Suponiendo que CartItems tiene un método getCartDto() que devuelve CartItemsDto
                .collect(Collectors.toList());

        OrderDto orderDto = new OrderDto();
        orderDto.setAmount(activeOrder.getAmount());
        orderDto.setId(activeOrder.getId());
        orderDto.setOrderStatus(activeOrder.getOrderStatus());
        orderDto.setDiscount(activeOrder.getDiscount());
        orderDto.setTotalAmount(activeOrder.getTotalAmount());

        // Manejo del cupón
        if (activeOrder.getCoupon() != null) {
            orderDto.setCouponName(activeOrder.getCoupon().getName());
        }

        orderDto.setCartItems(cartItemsDtoList);

        return orderDto;
    }


    public OrderDto applyCoupon(Long userId, String code){

        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(()-> new ValidationException("Coupon not found"));


        if(couponIsExpired(coupon)){
            throw new ValidationException("Coupon has expired");
        }

        double discountAmount = ((coupon.getDiscount()/100.0)*activeOrder.getTotalAmount());

        double netAmount = activeOrder.getTotalAmount() - discountAmount;

        activeOrder.setAmount((long)netAmount);
        activeOrder.setDiscount((long)discountAmount);
        activeOrder.setCoupon(coupon);

        orderRepository.save(activeOrder);
        return activeOrder.getOrderDto();
    }

    private boolean couponIsExpired(Coupon coupon){
       Date currentDate = new Date();
       Date expirationDate = coupon.getExpirationDate();

       return expirationDate != null && currentDate.after(expirationDate);
    }

    public OrderDto increaseProductQuantity(AddProductInCartDto addProductInCartDto){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);

        Optional<Product> optionalProduct = productRespository.findById(addProductInCartDto.getProductId());

        Optional<CartItems> optionalCartItems = cartsItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()

        );
        if(optionalProduct.isPresent() && optionalCartItems.isPresent()){

            CartItems cartItem = optionalCartItems.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() + product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() + product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() + 1);

            if(activeOrder.getCoupon() != null){
                double discountAmount = ((activeOrder.getCoupon().getDiscount()/100.0)*activeOrder.getTotalAmount());

                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long)netAmount);
                activeOrder.setDiscount((long)discountAmount);

            }

            cartsItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);

            return activeOrder.getOrderDto();
        }
    return null;
    }

    public OrderDto decreaseProductQuantity(AddProductInCartDto addProductInCartDto){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        Optional<Product> optionalProduct = productRespository.findById(addProductInCartDto.getProductId());
        Optional<CartItems> optionalCartItems = cartsItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()

        );
        if(optionalProduct.isPresent() && optionalCartItems.isPresent()){

            CartItems cartItem = optionalCartItems.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() - product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() - product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() - 1);

            if(activeOrder.getCoupon() != null){
                double discountAmount = ((activeOrder.getCoupon().getDiscount()/100.0)*activeOrder.getTotalAmount());

                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long)netAmount);
                activeOrder.setDiscount((long)discountAmount);

            }

            cartsItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);

            return activeOrder.getOrderDto();
        }
        return null;
    }

    public OrderDto placeOrder(PlaceOrderDto placeOrderDto) {
        // Buscar la orden activa
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(placeOrderDto.getUserId(), OrderStatus.Pending);
        Optional<User> optionalUser = userRepository.findById(placeOrderDto.getUserId());

        if (optionalUser.isPresent() && activeOrder != null) {
            User user = optionalUser.get();

            // Actualizar el pedido activo con los nuevos detalles
            activeOrder.setOrderDescription(placeOrderDto.getOrderDescription());
            activeOrder.setAddress(placeOrderDto.getAddress());
            activeOrder.setDate(new Date());
            activeOrder.setOrderStatus(OrderStatus.Placed);
            activeOrder.setTrackingId(UUID.randomUUID());

            try {
                orderRepository.save(activeOrder);

                // Verificar si ya existe una orden pendiente para el usuario
                if (!orderRepository.existsByUserIdAndOrderStatus(user.getId(), OrderStatus.Pending)) {
                    // Crear una nueva orden pendiente
                    Order newOrder = new Order();
                    newOrder.setAmount(0L);
                    newOrder.setTotalAmount(0L);
                    newOrder.setDiscount(0L);
                    newOrder.setUser(user);
                    newOrder.setOrderStatus(OrderStatus.Pending);
                    orderRepository.save(newOrder);
                }

                return activeOrder.getOrderDto();
            } catch (Exception e) {
                // Manejo de excepciones
                System.err.println("Error placing order: " + e.getMessage());
                throw new RuntimeException("Failed to place order", e);
            }
        }

        throw new RuntimeException("User not found or no active order found");
    }

    public List<OrderDto> getMyPlacedOrders(Long userId){

        return orderRepository.findByUserIdAndOrderStatusIn(userId,List.of(OrderStatus.Placed,OrderStatus.Shipped,
                OrderStatus.Delivered)).stream().map(Order::getOrderDto).collect(Collectors.toList());
    }

    public OrderDto searchOrderByTrackingId(UUID trackingId){
        Optional<Order> optionalOrder = orderRepository.findByTrackingId(trackingId);

        if(optionalOrder.isPresent()){
            return optionalOrder.get().getOrderDto();

        }
        return null;
    }

}

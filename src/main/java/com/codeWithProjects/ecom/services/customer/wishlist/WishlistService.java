package com.codeWithProjects.ecom.services.customer.wishlist;


import com.codeWithProjects.ecom.dto.WishlistDto;

import java.util.List;

public interface WishlistService {

    public WishlistDto addProductToWishlist(WishlistDto wishlistDto);

    public List<WishlistDto> getWishlistByUserId(Long userId);
}

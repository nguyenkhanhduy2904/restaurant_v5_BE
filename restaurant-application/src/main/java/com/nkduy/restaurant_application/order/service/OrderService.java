package com.nkduy.restaurant_application.order.service;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.branch.entity.BranchProduct;
import com.nkduy.restaurant_application.branch.entity.BranchProductStatus;
import com.nkduy.restaurant_application.branch.repository.BranchProductRepository;
import com.nkduy.restaurant_application.branch.repository.BranchRepository;
import com.nkduy.restaurant_application.config.util.OrderStatusStateMachine;
import com.nkduy.restaurant_application.config.util.PaymentStatusStateMachine;
import com.nkduy.restaurant_application.config.util.RefundStatusStateMachine;
import com.nkduy.restaurant_application.mapper.OrderMapper;
import com.nkduy.restaurant_application.order.dto.model.OrderDto;
import com.nkduy.restaurant_application.order.dto.model.OrderItemDto;
import com.nkduy.restaurant_application.order.dto.model.OrderValidationResult;
import com.nkduy.restaurant_application.order.dto.model.ValidatedOrderItem;
import com.nkduy.restaurant_application.order.dto.request.CreateOrderItemRequest;
import com.nkduy.restaurant_application.order.dto.request.CreateOrderRequest;
import com.nkduy.restaurant_application.order.dto.request.PreviewOrderRequest;
import com.nkduy.restaurant_application.order.dto.request.UpdateOrderStatusRequest;
import com.nkduy.restaurant_application.order.dto.response.CreateOrderResponse;
import com.nkduy.restaurant_application.order.dto.response.PreviewOrderResponse;
import com.nkduy.restaurant_application.order.dto.response.UpdateOrderStatusResponse;
import com.nkduy.restaurant_application.order.entity.Order;
import com.nkduy.restaurant_application.order.entity.OrderItem;
import com.nkduy.restaurant_application.order.entity.Payment;
import com.nkduy.restaurant_application.order.repository.OrderRepository;
import com.nkduy.restaurant_application.order.repository.OrderitemRepository;
import com.nkduy.restaurant_application.order.repository.PaymentRepository;
import com.nkduy.restaurant_application.product.entity.Product;
import com.nkduy.restaurant_application.product.repository.ProductRepository;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserDeliveryVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserOrderVoucher;
import com.nkduy.restaurant_application.voucher.service.DeliveryVoucherService;
import com.nkduy.restaurant_application.voucher.service.OrderVoucherService;
import com.nkduy.restaurant_application.voucher.service.UserOrderVoucherService;
import com.nkduy.restaurant_application.voucher.service.pricing.PricingService;
import com.nkduy.restaurant_application.voucher.service.pricing.model.Cart;
import com.nkduy.restaurant_application.voucher.service.pricing.model.CartLine;
import com.nkduy.restaurant_application.voucher.service.pricing.model.PriceBreakDown;
import com.nkduy.restaurant_application.voucher.service.pricing.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderitemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final BranchProductRepository branchProductRepository;

    private final DeliveryVoucherService deliveryVoucherService;
    private final OrderVoucherService orderVoucherService;

    private final OrderMapper orderMapper;


    private final DeliveryService deliveryService;
    private final PricingService pricingService;
    private final UserOrderVoucherService userOrderVoucherService;

    public PreviewOrderResponse previewOrder(PreviewOrderRequest request, String userId) {
        OrderValidationResult result = validateOrder(
                userId,
                request.branchId(),
                request.orderVoucherId(),
                request.item(),
                request.longitude(),
                request.latitude()
        );
        return result.toPreviewResponse();
    }

    public OrderValidationResult validateOrder(
            String userId,
            String branchId,
            String orderVoucherId,
            List<CreateOrderItemRequest> items,
            BigDecimal customerLongitude,
            BigDecimal customerLatitude
    ) {
        List<String> errors = new ArrayList<>();

        // 1. Basic entity existence & loading
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        if (user == null) {
            errors.add("User not valid");
        }

        Branch branch = branchId != null ? branchRepository.findById(branchId).orElse(null) : null;
        if (branch == null) {
            errors.add("Branch not valid");
        }

        if (items == null || items.isEmpty()) {
            errors.add("Order items cannot be empty");
        }

        if (!errors.isEmpty()) {
            return OrderValidationResult.invalid(errors);
        }

        // 2. Validate products and calculate order subtotal
        //
        List<ValidatedOrderItem> validatedItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItemCount = 0;

        for (CreateOrderItemRequest itemRequest : items) {
            Product product = productRepository.findById(itemRequest.productId()).orElse(null);
            if (product == null || !isProductAvailableAtBranch(product, branchId)) {
                errors.add("Product " + itemRequest.productId() + " is not available");
                continue;
            }

            CartLine cartLine = new CartLine(
                    String.valueOf(validatedItems.size()),
                    product.getId(),
                    product.getName(),
                    product.getUnitPrice(),
                    itemRequest.quantity(),
                    product.getAllTagIds()

            );

            validatedItems.add(new ValidatedOrderItem(product, cartLine, itemRequest.note()));

        }

        // 3. Calculate distance and delivery fee (with safe error handling)
        BigDecimal deliveryDistance = BigDecimal.ZERO;
        BigDecimal deliveryFee = BigDecimal.ZERO;
        try {
            deliveryDistance = deliveryService.calculateDistance(branch.getLongitude(), branch.getLatitude(), customerLongitude, customerLatitude);
            deliveryFee = deliveryService.calculateDeliveryFee(deliveryDistance);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        if (!errors.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, String.join(", ", errors));

        // 4. Validate vouchers and calculate discounts

        Cart cart = new Cart(validatedItems.stream().map(ValidatedOrderItem::cartLine).toList(), "", branchId, deliveryFee);//TODO: work out what should be done with orderType

        UserOrderVoucher userOrderVoucher = null;
        String voucherError = null;
        if(orderVoucherId != null && !orderVoucherId.isBlank()){
            userOrderVoucher = userOrderVoucherService.getUsableVoucher(orderVoucherId, userId);
            if(userOrderVoucher == null){
                voucherError = "VOUCHER_NOT_USABLE";
            }
        }

        PriceBreakDown priceBreakDown;
        if (voucherError != null) {
            priceBreakDown = PriceBreakDown.rejected(cart, voucherError);            // full price + a reason
        }
        else {
            OrderVoucher orderVoucher = userOrderVoucher != null ? userOrderVoucher.getVoucher() : null; //if userOrderVoucher not null, orderVoucher = userOrderVoucher.getVoucher, else null

            priceBreakDown = pricingService.calculate(cart, orderVoucher, loadUserContext(userId));

        }
        if(priceBreakDown.rejectReason() != null){
            userOrderVoucher = null;
        }

        totalItemCount = validatedItems.stream().mapToInt(i -> i.cartLine().quantity()).sum();

        return new OrderValidationResult(
                true,
                errors,
                user,
                branch,
                validatedItems,
                totalItemCount,
                deliveryDistance,
                userOrderVoucher,
                priceBreakDown,
                voucherError

        );



//        BigDecimal orderDiscount = BigDecimal.ZERO;
//        if (orderVoucherId != null && !orderVoucherId.isBlank()) {
//            if (!orderVoucherService.isVoucherUsable(orderVoucherId, userId, totalPrice)) {
//                errors.add("Order voucher is not valid or does not meet minimum order price");
//            } else {
//                orderDiscount = calculateOrderDiscount(orderVoucherId, totalPrice);
//            }
//        }
//
//        BigDecimal deliveryDiscount = BigDecimal.ZERO;
//        if (deliveryVoucherId != null && !deliveryVoucherId.isBlank()) {
//            if (!deliveryVoucherService.isVoucherUsable(deliveryVoucherId, userId, deliveryFee)) {
//                errors.add("Delivery voucher is not valid or does not meet minimum delivery fee");
//            } else {
//                deliveryDiscount = calculateDeliveryDiscount(deliveryVoucherId, deliveryFee);
//            }
//        }

//        BigDecimal finalPrice = totalPrice.add(deliveryFee)
//                .subtract(orderDiscount)
//                .subtract(deliveryDiscount)
//                .max(BigDecimal.ZERO);
//
//        return new OrderValidationResult(
//                errors.isEmpty(),
//                errors,
//                user,
//                branch,
//                validatedItems,
//                totalItemCount,
//                totalPrice,
//                deliveryDistance,
//                deliveryFee,
//                orderDiscount,
//                deliveryDiscount,
//                finalPrice
//        );
    }

    private UserContext loadUserContext(String userId) {
        return new UserContext(
                userId,
                false,
                BigDecimal.valueOf(100000l),
                "bronze"

        );
        //TODO: this is a place holder data, need work
    }

    //    private boolean isProductAvailableAtBranch(Product product, String branchId) {
//        // TODO: Check if product is unavailable at the specified branch( a blacklisted product or out of stock)
//        BranchProduct branchProduct = branchProductRepository.findByBranchIdAndProductId(branchId, product.getId()).orElse(null);
//        if (branchProduct != null && "UNAVAILABLE".equalsIgnoreCase(branchProduct.getStatus())) {
//            return false;
//        }
//        return true;
//    }
    private boolean isProductAvailableAtBranch(Product product, String branchId) {
        BranchProduct branchProduct = branchProductRepository.findByBranchIdAndProductId(branchId, product.getId()).orElse(null);
        if (branchProduct != null && branchProduct.getStatus() == BranchProductStatus.UNAVAILABLE) {
            return false;
        }
        return true;
    }



//    private BigDecimal getDistance(
//            Branch branch,
//            BigDecimal customerLongitude,
//            BigDecimal customerLatitude
//    ) {
//
//
//        if (branch == null
//                || customerLongitude == null
//                || customerLatitude == null
//                || branch.getLongitude() == null
//                || branch.getLatitude() == null) {
//            throw new IllegalArgumentException(
//                    "Branch or delivery coordinates are invalid"
//            );
//        }
//
//        double lat1 = Math.toRadians(branch.getLatitude().doubleValue());
//        double lon1 = Math.toRadians(branch.getLongitude().doubleValue());
//        double lat2 = Math.toRadians(customerLatitude.doubleValue());
//        double lon2 = Math.toRadians(customerLongitude.doubleValue());
//
//        double deltaLat = lat2 - lat1;
//        double deltaLon = lon2 - lon1;
//
//        double a = Math.pow(Math.sin(deltaLat / 2), 2)
//                + Math.cos(lat1)
//                * Math.cos(lat2)
//                * Math.pow(Math.sin(deltaLon / 2), 2);
//
//        // Prevent floating-point rounding from making a slightly
//        // negative value or a value greater than 1.
//        a = Math.max(0.0, Math.min(1.0, a));
//
//        double c = 2 * Math.atan2(
//                Math.sqrt(a),
//                Math.sqrt(1.0 - a)
//        );
//
//        double earthRadiusKm = 6371.0;
//        double distanceKm = earthRadiusKm * c;
//
//        return BigDecimal.valueOf(distanceKm)
//                .setScale(2, RoundingMode.HALF_UP);
//    }

//    private BigDecimal calculateDeliveryFee(BigDecimal deliveryDistance) {
//        if(deliveryDistance.compareTo(BigDecimal.valueOf(1)) < 0){
//            return BigDecimal.ZERO;
//        }
//        if(deliveryDistance.compareTo(BigDecimal.valueOf(5)) <= 0){
//            return BigDecimal.valueOf(10_000);
//        }
//        if(deliveryDistance.compareTo(BigDecimal.valueOf(10)) <= 0){
//            BigDecimal extraKm = deliveryDistance.subtract(BigDecimal.valueOf(5)).setScale(0, RoundingMode.CEILING);
//            return BigDecimal.valueOf(10_000).add(extraKm.multiply(BigDecimal.valueOf(3000)));
//
//        }
//        if(deliveryDistance.compareTo(BigDecimal.valueOf(20)) <= 0){
//            BigDecimal extraKm = deliveryDistance.subtract(BigDecimal.valueOf(10)).setScale(0, RoundingMode.CEILING);
//            return BigDecimal.valueOf(25_000).add(extraKm.multiply(BigDecimal.valueOf(5000)));
//
//        }
//        throw new IllegalArgumentException(
//                "Delivery address is outside the delivery range"
//        );
//
//    }

//    private BigDecimal calculateOrderDiscount(String voucherId, BigDecimal totalPrice) {
//        OrderVoucher orderVoucher = orderVoucherService.getOrderVoucherById(voucherId);
//        if (orderVoucher == null) return BigDecimal.ZERO;
//
//        BigDecimal discount = BigDecimal.ZERO;
//        if ("PERCENTAGE".equalsIgnoreCase(orderVoucher.getDiscountType())) {
//            discount = totalPrice.multiply(orderVoucher.getDiscountValue())
//                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//            if (orderVoucher.getMaxDiscount() != null && orderVoucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
//                discount = discount.min(orderVoucher.getMaxDiscount());
//            }
//        } else if ("FIXED".equalsIgnoreCase(orderVoucher.getDiscountType())) {
//            discount = orderVoucher.getDiscountValue();
//        }
//        return discount.min(totalPrice);
//    }
//
//    private BigDecimal calculateDeliveryDiscount(String voucherId, BigDecimal deliveryFee) {
//        DeliveryVoucher deliveryVoucher = deliveryVoucherService.getDeliveryVoucherById(voucherId);
//        if (deliveryVoucher == null) return BigDecimal.ZERO;
//
//        BigDecimal discount = BigDecimal.ZERO;
//        if ("PERCENTAGE".equalsIgnoreCase(deliveryVoucher.getDiscountType())) {
//            discount = deliveryFee.multiply(deliveryVoucher.getDiscountValue())
//                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//            if (deliveryVoucher.getMaxDiscount() != null && deliveryVoucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
//                discount = discount.min(deliveryVoucher.getMaxDiscount());
//            }
//        } else if ("FIXED".equalsIgnoreCase(deliveryVoucher.getDiscountType())) {
//            discount = deliveryVoucher.getDiscountValue();
//        }
//        return discount.min(deliveryFee);
//    }

//    private BigDecimal calculateOrderPrice(List<CreateOrderItemRequest> items) {
//        BigDecimal totalPrice = BigDecimal.ZERO;
//        for (CreateOrderItemRequest item : items) {
//            Product product = productRepository.findById(item.productId())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
//            totalPrice = totalPrice.add(product.getUnitPrice().multiply(BigDecimal.valueOf(item.quantity())));
//        }
//        return totalPrice;
//    }



    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, String userId) {
        if (request == null || request.previewOrderRequest() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order request");
        }

        if (request.contactPhone() == null || request.contactPhone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact phone is required");
        }
        if (request.contactName() == null || request.contactName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact name is required");
        }

        // 1. Validate order and calculate prices
        OrderValidationResult validation = validateOrder(
                userId,
                request.previewOrderRequest().branchId(),
                request.previewOrderRequest().orderVoucherId(),
//                request.previewOrderRequest().deliveryVoucherId(),
                request.previewOrderRequest().item(),
                request.previewOrderRequest().longitude(),
                request.previewOrderRequest().latitude()
        );

        if (!validation.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join(", ", validation.errors()));
        }

        User user = validation.user();
        Branch branch = validation.branch();
        List<ValidatedOrderItem> items = validation.items();
        BigDecimal finalPrice = validation.price().finalTotal();

        // 2. Fetch User Voucher references (if provided)

        if (validation.voucherError() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Voucher cannot be used: " + validation.voucherError());
        }
        UserOrderVoucher userOrderVoucher = validation.userVoucher();    // null when no voucher was sent
        if (userOrderVoucher != null) {
            userOrderVoucherService.markUse(userOrderVoucher);
        }


//        UserOrderVoucher userOrderVoucher = null;
//        if (request.previewOrderRequest().orderVoucherId() != null && !request.previewOrderRequest().orderVoucherId().isBlank()) {
//            userOrderVoucher = orderVoucherService.useOrderVoucher(request.previewOrderRequest().orderVoucherId(), user.getId());
//        }
//
//        UserDeliveryVoucher userDeliveryVoucher = null;
//        if (request.previewOrderRequest().deliveryVoucherId() != null && !request.previewOrderRequest().deliveryVoucherId().isBlank()) {
//            userDeliveryVoucher = deliveryVoucherService.useDeliveryVoucher(request.previewOrderRequest().deliveryVoucherId(), user.getId());
//        }

        // 3. Create and save Order
        Order newOrder = Order.builder()
                .priceBeforeDiscount(validation.price().total())
                .totalItem(validation.totalItemCount())

                .address(request.previewOrderRequest().address())
                .longitude(request.previewOrderRequest().longitude())
                .latitude(request.previewOrderRequest().latitude())

                .contactPhone(request.contactPhone())
                .contactName(request.contactName())
                .user(user)
                .deliveryFee(validation.price().deliveryFee())
                .deliveryDistance(validation.deliveryDistance())
                .orderDiscounted(validation.price().itemTotalDiscount())
                .deliveryDiscount(validation.price().deliveryDiscount())
                .branch(branch)
                .userOrderVoucher(userOrderVoucher)
                .userDeliveryVoucher(null)//TODO: remove this
                .finalPrice(validation.price().finalTotal())
                .build();

        Order savedOrder = orderRepository.save(newOrder);

        // 4. Create and save OrderItems
        for (ValidatedOrderItem item : items) {
            CartLine line = item.cartLine();
            BigDecimal discount = validation.price().discountPerLine().getOrDefault(line.id(), BigDecimal.ZERO);
            OrderItem newItem = OrderItem.builder()
                    .name(item.product().getName())
                    .quantity(line.quantity())
                    .subtotal(line.total())
                    .discounted(discount)
                    .unitPrice(line.unitPrice())
                    .order(savedOrder)
                    .product(item.product())
                    .note(item.note())
                    .build();
            orderItemRepository.save(newItem);
        }

        // 5. Create Payment
        Payment payment = Payment.builder()
                .paymentMethod(request.paymentMethod())
                .paymentStatus(request.paymentStatus())
                .order(savedOrder)
                .amount(finalPrice)
                .transactionRef(request.transactionRef())
                .build();
        paymentRepository.save(payment);

        return new CreateOrderResponse(
                orderMapper.toOrderDto(savedOrder, items.stream().map(orderMapper::toOrderItemDto).toList())
        );
        //TODO: Update ingredient
    }


    public OrderDto getOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));//get all the order
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());//get all the order item


        return orderMapper.toOrderDto(order, items.stream().map(orderMapper::toOrderItemDto).toList());

    }

    public Page<OrderDto> getOrderHistory(String userId, String status, Pageable pageable) {
        Page<Order> orders;
        //TODO: a way to check if status string is valid
        if(status == null || status.isBlank()){
            orders = orderRepository.findByUserId(userId, pageable);//get all the order
        }
        else{
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);//get all the order with status
        }


        if (orders.isEmpty()) { //if the order list is empty, return empty page, dont need to load the order item
            return orders.map(order ->
                    orderMapper.toOrderDto(order, List.of())
            );
        }

        List<String> orderIds = orders.getContent().stream().map(Order::getId).toList(); //get all the order id from the order list


        List<OrderDto> orderDtos = new ArrayList<>(); //create a new list to store the order dto
        List<OrderItem> orderItems = orderItemRepository.findByOrder_IdIn(orderIds);//get all the order of all the order id (number of order * number of order item)

        for(Order order : orders.getContent()){
            List<OrderItemDto> itemDtos = new ArrayList<>(); //create a new list to store the order item dto of this specific order

            for(OrderItem item : orderItems){
                if(item.getOrder().getId().equals(order.getId())){ //check if the orderItem id is the same as the order id
                    itemDtos.add(orderMapper.toOrderItemDto(item));
                }
            }
            OrderDto orderDto = orderMapper.toOrderDto(order, itemDtos); // create the order dto with the already populated order item dto
            orderDtos.add(orderDto); //add the order dto to the order dto list

        }
        return new PageImpl<>(orderDtos, pageable, orders.getTotalElements());

    }

    @Transactional
    public String cancelOrder(String id, String userId) {
        Order order = orderRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if(!order.getUser().getId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to cancel this order");
        }

        if(!OrderStatusStateMachine.canTransition(order.getStatus(), "CANCELLED")){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order cannot be cancelled in its current state");
        }

//        if(!order.getStatus().equals("PENDING")){
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order cannot be cancelled in its current state");
//        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        // Update Payment
        handleOrderCancel(order);


        //Update Voucher
        UserOrderVoucher userOrderVoucher = order.getUserOrderVoucher();
        if(userOrderVoucher != null && userOrderVoucher.getStatus().equals("USED")){
            orderVoucherService.cancelOrderVoucher(userOrderVoucher.getId());
        }

        UserDeliveryVoucher userDeliveryVoucher = order.getUserDeliveryVoucher();
        if(userDeliveryVoucher != null && userDeliveryVoucher.getStatus().equals("USED")){
            deliveryVoucherService.cancelDeliveryVoucher(userDeliveryVoucher.getId());
        }

        return "Order cancelled successfully";

    }

    @Transactional
    public void handleOrderCancel(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if(!PaymentStatusStateMachine.canTransition(payment.getPaymentStatus(), "CANCELLED")){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment status cannot be change in its current state");
        }
        if(!RefundStatusStateMachine.canTransition(payment.getRefundStatus(), "PENDING")){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Refund status cannot be change in its current state");
        }
        payment.setPaymentStatus("CANCELLED");
        payment.setRefundStatus("PENDING");
        paymentRepository.save(payment);
    }

    public Page<OrderDto> getOrders(String status, Pageable pageable) {
        Page<Order> orders;
        if(status == null || status.isBlank()){
            orders = orderRepository.findAll(pageable);
        }
        else{
            orders = orderRepository.findByStatus(status, pageable);
        }

        if (orders.isEmpty()) { //if the order list is empty, return empty page, dont need to load the order item
            return orders.map(order ->
                    orderMapper.toOrderDto(order, List.of())
            );
        }


        List<String> orderIds = orders.getContent().stream().map(Order::getId).toList(); //get all the order id from the order list

        List<OrderDto> orderDtos = new ArrayList<>(); //create a new list to store the order dto
        List<OrderItem> orderItems = orderItemRepository.findByOrder_IdIn(orderIds);//get all the order of all the order id (number of order * number of order item)


        for(Order order : orders.getContent()){
            List<OrderItemDto> itemDtos = new ArrayList<>(); //create a new list to store the order item dto of this specific order

            for(OrderItem item : orderItems){
                if(item.getOrder().getId().equals(order.getId())){ //check if the orderItem id is the same as the order id
                    itemDtos.add(orderMapper.toOrderItemDto(item));
                }
            }
            OrderDto orderDto = orderMapper.toOrderDto(order, itemDtos); // create the order dto with the already populated order item dto
            orderDtos.add(orderDto); //add the order dto to the order dto list

        }
        return new PageImpl<>(orderDtos, pageable, orders.getTotalElements());

    }

    @Transactional
    public UpdateOrderStatusResponse updateOrderStatus(UpdateOrderStatusRequest status, String id) {
        Order order = orderRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if(!OrderStatusStateMachine.canTransition(order.getStatus(), status.status())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order cannot be updated to this status");
        }
        order.setStatus(status.status());
        orderRepository.save(order);
        return new UpdateOrderStatusResponse(order.getId(), order.getStatus());
    }
}

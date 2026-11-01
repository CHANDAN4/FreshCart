package com.app.freshcart.common

enum class UserRole {
    ADMIN,
    USER
}

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    CONFIRMED,
    FAILED
}

enum class PaymentMethod {
    COD
}

enum class UnitType(val displayName: String) {
    KG("kg"),
    GRAM("g"),
    LITRE("L"),
    PACKET("pkt"),
    PIECE("pc")
}

package com.app.freshcart.data.local

import androidx.room.TypeConverter
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentMethod
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.common.UnitType
import com.app.freshcart.common.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole?): String? = role?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let { enumValueOf<UserRole>(it) }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus?): String? = status?.name

    @TypeConverter
    fun toOrderStatus(value: String?): OrderStatus? = value?.let { enumValueOf<OrderStatus>(it) }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus?): String? = status?.name

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? = value?.let { enumValueOf<PaymentStatus>(it) }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let { enumValueOf<PaymentMethod>(it) }

    @TypeConverter
    fun fromUnitType(unit: UnitType?): String? = unit?.name

    @TypeConverter
    fun toUnitType(value: String?): UnitType? = value?.let { enumValueOf<UnitType>(it) }
}

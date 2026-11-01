package com.app.freshcart.navigation

sealed class Screen(val route: String) {
    // Auth & Launch
    data object Splash : Screen("splash_screen")
    data object Login : Screen("login_screen")

    // Admin Routes
    data object AdminDashboard : Screen("admin_dashboard_screen")
    data object AdminCategories : Screen("admin_categories_screen")
    data object AdminProducts : Screen("admin_products_screen")
    data object AdminOrders : Screen("admin_orders_screen")

    // User Routes
    data object UserHome : Screen("user_home_screen")
    data object UserCategoryProducts : Screen("user_category_products_screen/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Long, categoryName: String): String {
            val encodedName = java.net.URLEncoder.encode(categoryName, "UTF-8")
            return "user_category_products_screen/$categoryId/$encodedName"
        }
    }
    data object UserCart : Screen("user_cart_screen")
    data object UserCheckoutSuccess : Screen("user_checkout_success_screen/{orderId}") {
        fun createRoute(orderId: Long): String = "user_checkout_success_screen/$orderId"
    }
    data object UserOrders : Screen("user_orders_screen")
    data object UserProfile : Screen("user_profile_screen")
    data object UserEditProfile : Screen("user_edit_profile_screen")
}

package com.app.freshcart.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.freshcart.common.UserRole
import com.app.freshcart.presentation.admin.category.AdminCategoriesScreen
import com.app.freshcart.presentation.admin.dashboard.AdminDashboardScreen
import com.app.freshcart.presentation.admin.orders.AdminOrdersScreen
import com.app.freshcart.presentation.admin.product.AdminProductsScreen
import com.app.freshcart.presentation.auth.LoginScreen
import com.app.freshcart.presentation.splash.SplashScreen
import com.app.freshcart.presentation.user.cart.CartScreen
import com.app.freshcart.presentation.user.category.CategoryProductsScreen
import com.app.freshcart.presentation.user.checkout.CheckoutSuccessScreen
import com.app.freshcart.presentation.user.home.UserHomeScreen
import com.app.freshcart.presentation.user.orders.UserOrdersScreen
import com.app.freshcart.presentation.user.profile.EditProfileScreen
import com.app.freshcart.presentation.user.profile.UserProfileScreen

@Composable
fun FreshCartNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdminHome = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToUserHome = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = if (role == UserRole.ADMIN) {
                        Screen.AdminDashboard.route
                    } else {
                        Screen.UserHome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToCategories = {
                    navController.navigate(Screen.AdminCategories.route)
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.AdminProducts.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.AdminOrders.route)
                },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Admin Categories
        composable(Screen.AdminCategories.route) {
            AdminCategoriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin Products
        composable(Screen.AdminProducts.route) {
            AdminProductsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin Orders
        composable(Screen.AdminOrders.route) {
            AdminOrdersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // User Home
        composable(Screen.UserHome.route) {
            UserHomeScreen(
                onNavigateToCategory = { categoryId, categoryName ->
                    navController.navigate(Screen.UserCategoryProducts.createRoute(categoryId, categoryName))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.UserCart.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.UserProfile.route)
                }
            )
        }

        // User Category Products
        composable(
            route = Screen.UserCategoryProducts.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) {
            CategoryProductsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.UserCart.route) }
            )
        }

        // User Cart
        composable(Screen.UserCart.route) {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.UserCheckoutSuccess.createRoute(orderId)) {
                        popUpTo(Screen.UserCart.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.UserHome.route) { inclusive = true }
                    }
                }
            )
        }

        // User Checkout Success
        composable(
            route = Screen.UserCheckoutSuccess.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")?.toLongOrNull() ?: 0L
            CheckoutSuccessScreen(
                orderId = orderId,
                onNavigateToOrders = {
                    navController.navigate(Screen.UserOrders.route) {
                        popUpTo(Screen.UserHome.route)
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.UserHome.route) { inclusive = true }
                    }
                }
            )
        }

        // User Orders
        composable(Screen.UserOrders.route) {
            UserOrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.UserCart.route) }
            )
        }

        // User Profile
        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Screen.UserEditProfile.route) },
                onNavigateToOrders = { navController.navigate(Screen.UserOrders.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.UserHome.route) { inclusive = true }
                    }
                }
            )
        }

        // User Edit Profile
        composable(Screen.UserEditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

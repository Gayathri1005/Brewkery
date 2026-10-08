package com.brewkery.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brewkery.app.ui.cart.CartScreen
import com.brewkery.app.ui.detail.DetailScreen
import com.brewkery.app.ui.home.HomeScreen
import com.brewkery.app.ui.order.OrderScreen
import com.brewkery.app.viewmodel.CartViewModel
import com.brewkery.app.viewmodel.DetailViewModel
import com.brewkery.app.viewmodel.MenuUiState
import com.brewkery.app.viewmodel.MenuViewModel

object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{itemId}"
    const val CART = "cart"
    const val ORDER = "order"
    fun detail(itemId: Int) = "detail/$itemId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // Activity-scoped so the menu, cart and active order survive screen changes.
    val menuViewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
    val cartViewModel: CartViewModel = viewModel()

    val menuState by menuViewModel.uiState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val activeOrder by cartViewModel.activeOrder.collectAsStateWithLifecycle()

    // Delivery fee, tax rate and ETA come from the API's meta block.
    LaunchedEffect(menuState) {
        (menuState as? MenuUiState.Success)?.let { cartViewModel.configure(it.meta) }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                menuViewModel = menuViewModel,
                cartState = cartState,
                activeOrder = activeOrder,
                onItemClick = { navController.navigate(Routes.detail(it)) },
                onCartClick = { navController.navigate(Routes.CART) },
                onTrackClick = { navController.navigate(Routes.ORDER) }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { entry ->
            val itemId = entry.arguments?.getInt("itemId") ?: 0
            val detailViewModel: DetailViewModel = viewModel(factory = DetailViewModel.factory(itemId))
            DetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onAddToCart = { state ->
                    state.item?.let { cartViewModel.addToCart(it, state.size, state.milk, state.sugar, state.quantity) }
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                state = cartState,
                onBack = { navController.popBackStack() },
                onClear = cartViewModel::clear,
                onIncrease = cartViewModel::increase,
                onDecrease = cartViewModel::decrease,
                onRemove = cartViewModel::remove,
                onPlaceOrder = {
                    if (cartViewModel.placeOrder() != null) {
                        navController.navigate(Routes.ORDER) { popUpTo(Routes.HOME) }
                    }
                }
            )
        }

        composable(Routes.ORDER) {
            OrderScreen(
                order = activeOrder,
                onBackToMenu = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }
    }
}

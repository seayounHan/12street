
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import StockDeliveryManager from "./components/StockDeliveryManager"

import OrderManager from "./components/OrderManager"

import OrderStatus from "./components/OrderStatus"
import PromoteManager from "./components/PromoteManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/stockDeliveries',
                name: 'StockDeliveryManager',
                component: StockDeliveryManager
            },

            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/orderStatuses',
                name: 'OrderStatus',
                component: OrderStatus
            },
            {
                path: '/promotes',
                name: 'PromoteManager',
                component: PromoteManager
            },



    ]
})

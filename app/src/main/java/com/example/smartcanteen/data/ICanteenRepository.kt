package com.example.smartcanteen.data

import kotlinx.coroutines.flow.Flow

interface IInventoryRepository {
    val allItems: Flow<List<FoodItem>>
    suspend fun insertItem(item: FoodItem)
    suspend fun updateItem(item: FoodItem)
    suspend fun deleteItem(item: FoodItem)
}

interface ISalesRepository {
    val allSales: Flow<List<Sale>>
    val allStockRecords: Flow<List<StockRecord>>
    suspend fun recordSale(itemId: String, quantity: Int, userId: String, username: String)
    suspend fun insertStockRecord(record: StockRecord)
}

interface IUserRepository {
    val allUsers: Flow<List<User>>
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User)
    suspend fun deleteUser(user: User)
}

interface ICanteenRepository : IInventoryRepository, ISalesRepository, IUserRepository

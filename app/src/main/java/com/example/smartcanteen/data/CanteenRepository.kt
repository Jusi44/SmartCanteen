package com.example.smartcanteen.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Interface defining the behavior of the Canteen data source.
 * This is an OOP principle called Abstraction.
 */
interface CanteenRepository {
    val allItems: Flow<List<FoodItem>>
    val allSales: Flow<List<Sale>>
    val allStockRecords: Flow<List<StockRecord>>
    val allUsers: Flow<List<User>>

    suspend fun insertItem(item: FoodItem)
    suspend fun updateItem(item: FoodItem)
    suspend fun deleteItem(item: FoodItem)
    suspend fun recordSale(itemId: String, quantity: Int, userId: String, username: String)
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun insertStockRecord(record: StockRecord)
}

/**
 * Concrete implementation of CanteenRepository using Firebase Firestore.
 */
class FirestoreCanteenRepository : CanteenRepository {
    private val db = FirebaseFirestore.getInstance()
    private val itemsCollection = db.collection("food_items")
    private val salesCollection = db.collection("sales")
    private val stockRecordsCollection = db.collection("stock_records")
    private val usersCollection = db.collection("users")

    override val allItems: Flow<List<FoodItem>> = itemsCollection
        .snapshots()
        .map { snapshot -> snapshot.toObjects(FoodItem::class.java) }

    override val allSales: Flow<List<Sale>> = salesCollection
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Sale::class.java) }

    override val allStockRecords: Flow<List<StockRecord>> = stockRecordsCollection
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(StockRecord::class.java) }

    override val allUsers: Flow<List<User>> = usersCollection
        .snapshots()
        .map { snapshot -> snapshot.toObjects(User::class.java) }

    override suspend fun insertItem(item: FoodItem) {
        itemsCollection.add(item).await()
    }

    override suspend fun updateItem(item: FoodItem) {
        itemsCollection.document(item.id).set(item).await()
    }

    override suspend fun deleteItem(item: FoodItem) {
        itemsCollection.document(item.id).delete().await()
    }

    override suspend fun recordSale(itemId: String, quantity: Int, userId: String, username: String) {
        val itemDoc = itemsCollection.document(itemId)
        val itemSnapshot = itemDoc.get().await()
        val item = itemSnapshot.toObject(FoodItem::class.java) ?: return
        
        if (item.currentStock >= quantity) {
            val sale = Sale(
                itemId = itemId,
                itemName = item.name,
                quantitySold = quantity,
                totalPrice = item.price * quantity,
                userId = userId
            )
            
            db.runTransaction { transaction ->
                transaction.set(salesCollection.document(), sale)
                transaction.update(itemDoc, "currentStock", item.currentStock - quantity)
                
                val record = StockRecord(
                    itemId = itemId,
                    quantityChanged = quantity,
                    type = "SALE",
                    userId = userId,
                    username = username
                )
                transaction.set(stockRecordsCollection.document(), record)
            }.await()
        }
    }
    
    override suspend fun insertStockRecord(record: StockRecord) {
        stockRecordsCollection.add(record).await()
    }

    override suspend fun getUserByUsername(username: String): User? {
        val snapshot = usersCollection.whereEqualTo("username", username).limit(1).get().await()
        return snapshot.toObjects(User::class.java).firstOrNull()
    }

    override suspend fun insertUser(user: User) {
        usersCollection.add(user).await()
    }

    override suspend fun deleteUser(user: User) {
        usersCollection.document(user.id).delete().await()
    }
}

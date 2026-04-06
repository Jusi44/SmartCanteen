package com.example.smartcanteen.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CanteenRepository {
    private val db = FirebaseFirestore.getInstance()
    private val itemsCollection = db.collection("food_items")
    private val salesCollection = db.collection("sales")
    private val stockRecordsCollection = db.collection("stock_records")
    private val usersCollection = db.collection("users")

    val allItems: Flow<List<FoodItem>> = itemsCollection
        .snapshots()
        .map { snapshot -> snapshot.toObjects(FoodItem::class.java) }

    val allSales: Flow<List<Sale>> = salesCollection
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Sale::class.java) }

    val allStockRecords: Flow<List<StockRecord>> = stockRecordsCollection
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(StockRecord::class.java) }

    val allUsers: Flow<List<User>> = usersCollection
        .snapshots()
        .map { snapshot -> snapshot.toObjects(User::class.java) }

    suspend fun insertItem(item: FoodItem) {
        itemsCollection.add(item).await()
    }

    suspend fun updateItem(item: FoodItem) {
        itemsCollection.document(item.id).set(item).await()
    }

    suspend fun deleteItem(item: FoodItem) {
        itemsCollection.document(item.id).delete().await()
    }

    suspend fun recordSale(itemId: String, quantity: Int, userId: String, username: String) {
        val itemDoc = itemsCollection.document(itemId)
        val item = itemDoc.get().await().toObject(FoodItem::class.java) ?: return
        
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
    
    suspend fun insertStockRecord(record: StockRecord) {
        stockRecordsCollection.add(record).await()
    }

    suspend fun getUserByUsername(username: String): User? {
        val snapshot = usersCollection.whereEqualTo("username", username).limit(1).get().await()
        return snapshot.toObjects(User::class.java).firstOrNull()
    }

    suspend fun insertUser(user: User) {
        usersCollection.add(user).await()
    }

    suspend fun deleteUser(user: User) {
        usersCollection.document(user.id).delete().await()
    }
}

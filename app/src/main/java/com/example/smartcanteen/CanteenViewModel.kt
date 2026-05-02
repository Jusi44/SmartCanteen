package com.example.smartcanteen

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartcanteen.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CanteenViewModel(
    application: Application,
    private val repository: ICanteenRepository
) : AndroidViewModel(application) {

    val allItems: StateFlow<List<FoodItem>>
    val allSales: StateFlow<List<Sale>>
    
    private val _stockRecords = MutableStateFlow<List<StockRecord>>(emptyList())
    val allStockRecords: StateFlow<List<StockRecord>> = _stockRecords

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        allItems = repository.allItems
            .onEach { Log.d("CanteenApp", "Items updated: ${it.size}") }
            .catch { e -> Log.e("CanteenApp", "Error fetching items", e) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        allSales = repository.allSales
            .onEach { Log.d("CanteenApp", "Sales updated: ${it.size}") }
            .catch { e -> Log.e("CanteenApp", "Error fetching sales", e) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            try {
                // 1. Attempt to ensure we have a Firebase session
                try {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        Log.d("CanteenApp", "Attempting anonymous sign-in...")
                        FirebaseAuth.getInstance().signInAnonymously().await()
                        Log.d("CanteenApp", "Anonymous sign-in successful")
                    }
                } catch (authEx: Exception) {
                    Log.e("CanteenApp", "Anonymous Auth Failed. Make sure it's enabled in Firebase Console.", authEx)
                }

                // 2. Start listeners (even if auth failed, we try in case rules are public)
                launch {
                    repository.allStockRecords
                        .catch { e -> 
                            Log.e("CanteenApp", "Error fetching stock records", e)
                            if (e.message?.contains("PERMISSION_DENIED") == true) {
                                // Specific log for user
                                Log.e("CanteenApp", "CRITICAL: Firestore Rules are blocking access.")
                            }
                        }
                        .collect { _stockRecords.value = it }
                }

                launch {
                    repository.allUsers
                        .catch { e -> Log.e("CanteenApp", "Error fetching users", e) }
                        .collect { _allUsers.value = it }
                }

                // 3. Initialize default accounts if needed
                try {
                    val admin = repository.getUserByUsername("admin")
                    if (admin == null) {
                        repository.insertUser(User(username = "admin", password = "admin123", role = "ADMIN"))
                    }

                    val staff = repository.getUserByUsername("staff")
                    if (staff == null) {
                        repository.insertUser(User(username = "staff", password = "staff123", role = "STAFF"))
                    }
                } catch (dbEx: Exception) {
                    Log.e("CanteenApp", "Database init failed - check Firestore Rules", dbEx)
                }
            } catch (e: Exception) {
                Log.e("CanteenApp", "General error during initialization", e)
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val trimmedUsername = username.trim()
                val user = repository.getUserByUsername(trimmedUsername)
                if (user != null && user.password == password) {
                    _currentUser.value = user
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("CanteenApp", "Login Error", e)
                val errorMessage = e.message ?: "Unknown error"
                if (errorMessage.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    Toast.makeText(getApplication(), "Database Permission Error. Please check Firebase Rules.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(getApplication(), "Login Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun addUser(username: String, password: String, role: String) {
        viewModelScope.launch {
            try {
                repository.insertUser(User(username = username, password = password, role = role))
                Toast.makeText(getApplication(), "User Added", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CanteenApp", "Add User Error", e)
                Toast.makeText(getApplication(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                if (user.username != "admin") {
                    repository.deleteUser(user)
                    Toast.makeText(getApplication(), "User Deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CanteenApp", "Delete User Error", e)
            }
        }
    }

    fun addItem(name: String, category: String, price: Double, stock: Int, threshold: Int) {
        viewModelScope.launch {
            try {
                Log.d("CanteenApp", "Adding item: $name")
                val item = FoodItem(name = name, category = category, price = price, currentStock = stock, minThreshold = threshold)
                repository.insertItem(item)
                Toast.makeText(getApplication(), "Item Added: $name", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CanteenApp", "Add Item Error", e)
                Toast.makeText(getApplication(), "Error adding item: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateItem(item: FoodItem) {
        viewModelScope.launch {
            try {
                repository.updateItem(item)
                Toast.makeText(getApplication(), "Item Updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CanteenApp", "Update Item Error", e)
                Toast.makeText(getApplication(), "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteItem(item: FoodItem) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
                Toast.makeText(getApplication(), "Item Deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CanteenApp", "Delete Item Error", e)
            }
        }
    }
    
    fun restockItem(item: FoodItem, quantity: Int) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value
                val updatedItem = item.copy(currentStock = item.currentStock + quantity)
                repository.updateItem(updatedItem)
                repository.insertStockRecord(
                    StockRecord(
                        itemId = item.id, 
                        quantityChanged = quantity, 
                        type = "RESTOCK",
                        userId = user?.id ?: "",
                        username = user?.username ?: "System"
                    )
                )
                Toast.makeText(getApplication(), "Stock Updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CanteenApp", "Restock Error", e)
                Toast.makeText(getApplication(), "Restock Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun recordSale(item: FoodItem, quantity: Int) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value
                repository.recordSale(item.id, quantity, user?.id ?: "", user?.username ?: "System")
                Toast.makeText(getApplication(), "Sale Recorded!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CanteenApp", "Sale Error", e)
                Toast.makeText(getApplication(), "Sale Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CanteenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CanteenViewModel(application, CanteenRepository()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

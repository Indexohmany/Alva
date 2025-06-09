package com.example.alva.data.database.dao

import androidx.room.*
import com.example.alva.data.database.entities.ProductInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductInfoDao {
    @Query("SELECT * FROM product_info WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): ProductInfoEntity?

    @Query("SELECT * FROM product_info ORDER BY lastUpdated DESC")
    fun getAllProducts(): Flow<List<ProductInfoEntity>>

    @Query("SELECT * FROM product_info WHERE name LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchProducts(query: String): List<ProductInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductInfoEntity>)

    @Update
    suspend fun updateProduct(product: ProductInfoEntity)

    @Delete
    suspend fun deleteProduct(product: ProductInfoEntity)

    @Query("DELETE FROM product_info WHERE barcode = :barcode")
    suspend fun deleteProductByBarcode(barcode: String)

    @Query("DELETE FROM product_info")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM product_info WHERE lastUpdated < :cutoffTime")
    suspend fun deleteOldProducts(cutoffTime: Long)
}

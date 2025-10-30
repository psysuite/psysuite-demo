package iit.uvip.psysuite.network

import android.util.Log
import iit.uvip.psysuite.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Centralized HTTP client for all API communications
 * This makes it easy to switch between HTTP and HTTPS later
 */
object ApiClient {
    
    private const val TAG = "ApiClient"
    
    // Base configuration
    private val baseUrl: String = BuildConfig.API_URL
    private val apiKey: String = BuildConfig.API_KEY
    
    // Connection timeouts
    private const val CONNECT_TIMEOUT = 30000
    private const val READ_TIMEOUT = 60000
    
    /**
     * Create a configured HTTP connection for API endpoints
     */
    fun createConnection(endpoint: String): HttpURLConnection {
        val fullUrl = if (endpoint.startsWith("/")) {
            "$baseUrl$endpoint"
        } else {
            "$baseUrl/$endpoint"
        }
        
        Log.d(TAG, "Creating connection to: $fullUrl")
        
        val url = URL(fullUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        // Basic configuration
        connection.connectTimeout = CONNECT_TIMEOUT
        connection.readTimeout = READ_TIMEOUT
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "PsySuite-Android")
        
        return connection
    }
    
    /**
     * Create a POST connection with JSON content type and authorization
     */
    fun createPostConnection(endpoint: String): HttpURLConnection {
        val connection = createConnection(endpoint)
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.doOutput = true
        return connection
    }
    
    /**
     * Create a GET connection with authorization
     */
    fun createGetConnection(endpoint: String): HttpURLConnection {
        val connection = createConnection(endpoint)
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        return connection
    }
    
    /**
     * Test server connectivity
     */
    fun testConnectivity(endpoint: String): Boolean {
        return try {
            val connection = createConnection(endpoint)
            connection.connectTimeout = 5000
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            Log.d(TAG, "Connectivity test: HTTP $responseCode")
            responseCode in 200..299 || responseCode == 404 // 404 is OK, means server is reachable
        } catch (e: Exception) {
            Log.w(TAG, "Connectivity test failed", e)
            false
        }
    }
    
    /**
     * Get the base URL for logging/debugging
     */
    fun getBaseUrl(): String = baseUrl
}
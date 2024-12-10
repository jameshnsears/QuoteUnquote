import android.database.sqlite.SQLiteDatabase
import kotlin.math.sqrt

fun calculateCosineSimilarity(vec1: List<Float>, vec2: List<Float>): Float {
    val dotProduct = vec1.zip(vec2).sumOf { it.first * it.second }
    val magnitude = sqrt(vec1.sumOf { it * it }) * sqrt(vec2.sumOf { it * it })
    return if (magnitude == 0.0f) 0.0f else dotProduct / magnitude
}

fun querySimilarQuotes(dbPath: String, queryText: String, threshold: Float): List<Pair<String, Float>> {
    // Step 1: Open the database
    val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

    // Step 2: Compute the embedding for the query
    val queryEmbedding = getQueryEmbedding(queryText) // Implement your embedding logic here

    // Step 3: Query the database for all quotes and embeddings
    val cursor = db.rawQuery("SELECT text, embedding FROM Quotes", null)
    val results = mutableListOf<Pair<String, Float>>()

    while (cursor.moveToNext()) {
        val text = cursor.getString(0)
        val embeddingStr = cursor.getString(1)

        // Convert embedding string back to a list of floats
        val embedding = embeddingStr.split(",").map { it.toFloat() }

        // Calculate cosine similarity
        val similarity = calculateCosineSimilarity(queryEmbedding, embedding)

        if (similarity > threshold) {
            results.add(text to similarity)
        }
    }

    cursor.close()
    db.close()

    // Step 4: Sort and return results
    return results.sortedByDescending { it.second }
}

fun getQueryEmbedding(queryText: String): List<Float> {
    // Use TensorFlow Lite, DJL, or an API to generate the embedding for the query
    // Placeholder: Return a dummy embedding for demonstration purposes
    return listOf(0.1f, 0.2f, 0.3f, /*...*/)
}

// Example usage
fun main() {
    val dbPath = "path_to_quotes.db"
    val queryText = "Every difficulty brings opportunity."
    val threshold = 0.3f

    val similarQuotes = querySimilarQuotes(dbPath, queryText, threshold)
    println("Input Query: $queryText")
    println("Similar Quotes:")
    for ((quote, similarity) in similarQuotes) {
        println("$quote (Similarity: $similarity)")
    }
}

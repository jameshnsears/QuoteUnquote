import ai.djl.Model
import ai.djl.ModelZoo
import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.embedding.WordEmbedding
import ai.djl.translate.TranslateException

fun main() {
    val corpus = listOf(
        "The only limit to our realization of tomorrow is our doubts of today.",
        "In the middle of every difficulty lies opportunity.",
        "Success is not final, failure is not fatal: It is the courage to continue that counts."
    )

    val query = "Every difficulty brings opportunity."

    // Load pre-trained embeddings model (example: Universal Sentence Encoder or similar)
    val model = ModelZoo.loadModel("sentence_transformers/all-MiniLM-L6-v2")
    val embedder = model.newPredictor<String, FloatArray>()

    val queryEmbedding = embedder.predict(query)
    val similarities = mutableListOf<Pair<String, Float>>()

    for (quote in corpus) {
        val quoteEmbedding = embedder.predict(quote)
        val similarity = cosineSimilarity(queryEmbedding, quoteEmbedding)
        if (similarity > 0.2) {
            similarities.add(quote to similarity)
        }
    }

    println("Similar Quotes:")
    similarities.sortedByDescending { it.second }.forEach { println("${it.first} (Similarity: ${it.second})") }
}

fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
    val dotProduct = vec1.zip(vec2).sumOf { it.first * it.second }
    val magnitude = Math.sqrt(vec1.sumOf { it * it } * vec2.sumOf { it * it })
    return (dotProduct / magnitude).toFloat()
}

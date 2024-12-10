import org.apache.commons.text.similarity.CosineSimilarity;
import java.util.*;

public class QuoteSearcher {
    public static void main(String[] args) {
        // Corpus of quotations
        List<String> corpus = Arrays.asList(
            "The only limit to our realization of tomorrow is our doubts of today.",
            "In the middle of every difficulty lies opportunity.",
            "Success is not final, failure is not fatal: It is the courage to continue that counts."
        );

        // Input query
        String query = "Every difficulty brings opportunity.";

        // Preprocess corpus and query (example: lowercase and tokenization)
        Map<CharSequence, Integer> queryVector = createTFVector(query);
        List<String> results = new ArrayList<>();

        for (String quote : corpus) {
            Map<CharSequence, Integer> quoteVector = createTFVector(quote);
            double similarity = calculateCosineSimilarity(queryVector, quoteVector);

            // Filter results by a similarity threshold
            if (similarity > 0.2) { // Adjust the threshold as needed
                results.add(quote + " (Similarity: " + similarity + ")");
            }
        }

        System.out.println("Similar Quotes:");
        results.forEach(System.out::println);
    }

    private static Map<CharSequence, Integer> createTFVector(String text) {
        Map<CharSequence, Integer> vector = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            vector.put(word, vector.getOrDefault(word, 0) + 1);
        }
        return vector;
    }

    private static double calculateCosineSimilarity(Map<CharSequence, Integer> vec1, Map<CharSequence, Integer> vec2) {
        CosineSimilarity cosine = new CosineSimilarity();
        return cosine.cosineSimilarity(vec1, vec2);
    }
}

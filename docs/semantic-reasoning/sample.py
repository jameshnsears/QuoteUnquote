from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

# Initialize the Sentence Transformer model
model = SentenceTransformer('all-MiniLM-L6-v2')

# Corpus of quotations
corpus = [
    "The only limit to our realization of tomorrow is our doubts of today.",
    "In the middle of every difficulty lies opportunity.",
    "Success is not final, failure is not fatal: It is the courage to continue that counts.",
]

# Step 1: Precompute embeddings for the corpus
corpus_embeddings = model.encode(corpus)

# Function to find similar quotes
def find_similar_quotes(query, corpus, corpus_embeddings, threshold=0.3):
    # Embed the query
    query_embedding = model.encode([query])
    
    # Compute cosine similarities
    similarities = cosine_similarity(query_embedding, corpus_embeddings)

    # Retrieve and sort similar quotes
    results = [
        (corpus[idx], similarity)
        for idx, similarity in enumerate(similarities[0])
        if similarity > threshold
    ]
    results.sort(key=lambda x: x[1], reverse=True)
    
    return results

# Example query
query = "Every difficulty brings opportunity."

# Find similar quotes
similar_quotes = find_similar_quotes(query, corpus, corpus_embeddings)

# Display results
print("Input Query:")
print(query)
print("\nSimilar Quotes:")
for quote, score in similar_quotes:
    print(f"{quote} (Similarity: {score:.2f})")

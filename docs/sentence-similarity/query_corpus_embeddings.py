import sqlite3
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

# Initialize the Sentence Transformer model
# download it
# model = SentenceTransformer('all-MiniLM-L6-v2')

# use local copy
model = SentenceTransformer('./all-MiniLM-L6-v2')


# Function to calculate cosine similarity
def calculate_cosine_similarity(vec1, vec2):
    return cosine_similarity([vec1], [vec2])[0][0]

# Function to query similar quotes
def find_similar_quotes(query, db_file, threshold=0.3):
    # Step 1: Embed the query
    query_embedding = model.encode(query)

    # Step 2: Connect to the database
    conn = sqlite3.connect(db_file)
    cursor = conn.cursor()

    # Step 3: Retrieve all rows from the database
    cursor.execute("SELECT text, embedding FROM Quotes")
    rows = cursor.fetchall()

    results = []
    for text, embedding_str in rows:
        # Convert the embedding string back to a numpy array
        embedding = np.array([float(x) for x in embedding_str.split(",")])

        # Calculate cosine similarity
        similarity = calculate_cosine_similarity(query_embedding, embedding)

        if similarity > threshold:
            results.append((text, similarity))

    # Sort results by similarity in descending order
    results.sort(key=lambda x: x[1], reverse=True)

    # Close the database connection
    conn.close()

    return results

# Example usage
if __name__ == "__main__":
    db_file = "quotes.db"  # Path to the database file
    query = "Every difficulty brings opportunity."
    threshold = 0.3

    # Find similar quotes
    similar_quotes = find_similar_quotes(query, db_file, threshold)

    # Display results
    print("Input Query:")
    print(query)
    print("\nSimilar Quotes:")
    for quote, score in similar_quotes:
        print(f"{quote} (Similarity: {score:.2f})")

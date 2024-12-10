import sqlite3
from sentence_transformers import SentenceTransformer

# Initialize the Sentence Transformer model
model = SentenceTransformer("/home/jsears/GIT_REPOS/quoteunquote/docs/sentence-similarity/all-MiniLM-L6-v2')

# Corpus of quotations
corpus = [
    "The only limit to our realization of tomorrow is our doubts of today.",
    "In the middle of every difficulty lies opportunity.",
    "Success is not final, failure is not fatal: It is the courage to continue that counts.",
]

# Step 1: Compute embeddings for the corpus
corpus_embeddings = model.encode(corpus)

# Step 2: Create SQLite database
db_file = "quotes.db"
conn = sqlite3.connect(db_file)
cursor = conn.cursor()

# Create table
cursor.execute("""
CREATE TABLE IF NOT EXISTS Quotes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    text TEXT NOT NULL,
    embedding TEXT NOT NULL
)
""")

cursor.execute("CREATE INDEX IF NOT EXISTS idx_embeddings ON Quotes(embedding)")
cursor.execute("CREATE INDEX IF NOT EXISTS idx_text ON Quotes(text)")

# Step 3: Insert corpus and embeddings into the database
for text, embedding in zip(corpus, corpus_embeddings):
    # Convert embedding to comma-separated string
    embedding_str = ",".join(map(str, embedding))
    cursor.execute("INSERT INTO Quotes (text, embedding) VALUES (?, ?)", (text, embedding_str))

# Commit changes and close the connection
conn.commit()
conn.close()

print(f"Database created with embeddings stored in {db_file}")

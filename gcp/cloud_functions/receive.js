"use strict";

const { Storage } = require('@google-cloud/storage');
const storage = new Storage();

function receiveFromBucket(bucket, code, callback) {
  console.info("receiveFromBucket: code=" + code);

  const storageBucket = storage.bucket(bucket);

  const file = storageBucket.file(code);

  var retryCount = 3;
  
  file.download(function (err, digests) {
    if (err) {
      console.error("retryCount=" + retryCount+ "; err=" + err);
      retryCount --;
      if (retryCount === 0) {
        return callback("[]");
      } else {
        receiveFromBucket(bucket, code, callback);
      }
    } else {
      console.info("digests=" + digests);

      // requires that client unescapes!
      return callback(digests.toString());
    }
  });
}


////////////////////////////////////////////

const { Firestore } = require('@google-cloud/firestore');
const firestore = new Firestore();

async function receiveFromFirebase(collection, code, callback) {
  console.info("receiveFromFirebase: code=" + code);

  const document = firestore.doc(collection + "/" + code);

  document.get().then(doc => {
    if (!doc.exists) {
      console.error("doc does not exist")
    } else {
      return callback(doc.data().digests);
    }
  });
}

module.exports = { receiveFromBucket, receiveFromFirebase };

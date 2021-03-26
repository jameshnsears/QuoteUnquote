"use strict";

const { Storage } = require('@google-cloud/storage');
const storage = new Storage();

function loadFromBucket(bucket, code, callback) {
  console.info("loadFromBucket");

  const storageBucket = storage.bucket(bucket);

  const file = storageBucket.file(code);
  
  file.download(function (err, digests) {
    if (err) {
      console.error("err=" + err);
    } else {
      console.info("digests=" + digests);

      return callback(digests.toString());
    }
  });
}

module.exports = { loadFromBucket };

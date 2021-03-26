"use strict";

const { Storage } = require('@google-cloud/storage');
const storage = new Storage();

function saveToBucket(bucket, code, digests) {
    console.info("saveToBucket: code=" + code);

    const storageBucket = storage.bucket(bucket);

    const file = storageBucket.file(code);

    file.save(JSON.stringify(digests), function (err) {
        if (err) {
            console.error(err);
            return false;
        }
    });

    return true;
}

////////////////////////////////////////////

const { Firestore } = require('@google-cloud/firestore');
const firestore = new Firestore();

function saveToFirebase(collection, code, digests) {
    console.info("saveToFirebase: code=" + code);

    const document = firestore.doc(collection + "/" + code);

    var date = new Date();
    var now = date.getTime();

    document.set({
        now: now,
        digests: digests
      });

    return true;
}

module.exports = { saveToBucket, saveToFirebase };

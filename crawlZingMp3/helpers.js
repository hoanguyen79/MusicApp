async function deleteCollection(db, collectionPath) {
    const collectionRef = db.collection(collectionPath);
    const query = collectionRef.orderBy('__name__');

    return new Promise((resolve, reject) => {
        deleteQueryBatch(db, query, resolve).catch(reject);
    });
}

async function deleteQueryBatch(db, query, resolve) {
    const snapshot = await query.get();

    const batchSize = snapshot.size;

    if (batchSize === 0) {
        resolve();
        return;
    }

    const batch = db.batch();

    snapshot.docs.forEach((doc) => {
        batch.delete(doc.ref);
    });

    await batch.commit();

    process.nextTick(() => {
        deleteQueryBatch(db, query, resolve);
    });
}

async function insert(database, collectionName, documents) {
    const batch = database.batch();

    documents.forEach(document => {
        const docRef = database.collection(collectionName).doc();
        batch.set(docRef, document);
    });

    await batch.commit();
}

module.exports = {
    deleteCollection,
    insert,
};
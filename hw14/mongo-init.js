db.createUser({
    user: "root",
    pwd: "mongodb_secret",
    roles: [{
        role: "readWrite",
        db: "hw14-data-mongodb"
    }]
});
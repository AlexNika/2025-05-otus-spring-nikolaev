db.createUser({
    user: "root",
    pwd: "mongodb_secret",
    roles: [{
        role: "readWrite",
        db: "hw08-data-mongodb"
    }]
});
// Runs automatically on first start of the mongo container
db = db.getSiblingDB('item_db');
db.items.insertOne({
    sku: "SKU-001",
    name: "Blue Widget",
    description: "A nice widget",
    categories: ["widget", "blue"],
    price: 19.99,
    stock: 50,
    active: true,
    pictureUrl: null
});

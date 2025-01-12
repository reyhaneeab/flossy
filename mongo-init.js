// First, wait for MongoDB to be ready
db.waitForPrimaryOk();

// Try to initialize the replica set if not already initialized
try {
  rs.status();
} catch (err) {
  // If rs.status() fails, the replica set hasn't been initialized
  print("Initializing replica set...");
  rs.initiate({
    _id: "rs0",
    members: [
      {_id: 0, host: "mongodb1:27017"},
      {_id: 1, host: "mongodb2:27017"},
      {_id: 2, host: "mongodb3:27017"}
    ]
  });
}

// Wait for replica set to be fully initialized
let isReplicaSetReady = false;
while (!isReplicaSetReady) {
  try {
    const status = rs.status();
    if (status.ok === 1 && status.members.some(m => m.stateStr === "PRIMARY")) {
      isReplicaSetReady = true;
    } else {
      print("Waiting for replica set to be ready...");
      sleep(1000);
    }
  } catch (err) {
    print("Error checking replica set status, retrying...");
    sleep(1000);
  }
}

// Only proceed with data initialization if we're on the primary
if (rs.isMaster().ismaster) {
  print("On primary node, proceeding with initialization");

  // Switch to the flossyDB database
  db = db.getSiblingDB('flossyDB');

  // Create collections
  db.createCollection('users');
  db.createCollection('dentists');
  db.createCollection('appointments');

  // Insert initial data
  try {
    db.users.insertMany([
      {
        username: 'user1',
        email: 'user1@example.com'
      },
      {
        username: 'user2',
        email: 'user2@example.com'
      }
    ]);

    db.dentists.insertMany([
      {
        name: 'Dr. Smith'
      },
      {
        name: 'Dr. Johnson'
      }
    ]);

    // Create indexes
    db.users.createIndex({ "email": 1 }, { unique: true });
    db.dentists.createIndex({ "name": 1 });

    print("Database initialization completed successfully");
  } catch (err) {
    print("Error during data initialization: " + err.message);
  }
} else {
  print("Not on primary node, skipping initialization");
}
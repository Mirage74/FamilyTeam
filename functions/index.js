const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { CloudTasksClient } = require("@google-cloud/tasks");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const jwt = require('jsonwebtoken');

const client = new CloudTasksClient();

admin.initializeApp();

const projectId = "family-team-e3e00";
const location = "us-central1";
const queue = "notifications-queue";

const createTask = async (id, description, deviceToken, alarmTime) => {
  
  const url = `https://${location}-${projectId}.cloudfunctions.net/sendNotificationTask`;


  const scheduleTimeInSeconds = Math.floor(alarmTime / 1000);

  if (scheduleTimeInSeconds <= Date.now() / 1000) {
    console.log(`Alarm time for ${id} has already passed.`);
    return null;
  }

  const base64Body = Buffer.from(JSON.stringify({ id, description, deviceToken }), 'utf-8').toString("base64");
 

  if (!deviceToken || !/^[a-zA-Z0-9-_:/.]+$/.test(deviceToken)) {
    console.error(`Invalid device token format: ${deviceToken}`);
    return;
  }


  const task = {
    httpRequest: {
      httpMethod: "POST",
      url,
      headers: {
        "Content-Type": "application/json",
      },
      oidcToken: {
        serviceAccountEmail: "family-team-e3e00@appspot.gserviceaccount.com",
      },
      body: base64Body
    },
    scheduleTime: {
      seconds: scheduleTimeInSeconds
    }
  };

  try {
    const response = await client.createTask({
      parent: client.queuePath(projectId, location, queue),
      task,
    });

    const collectionName = "reminders-in-queue"
    const taskId = response[0]?.name?.split('/').pop();

    const documentId = String(id);

    if (!documentId || typeof documentId !== "string" || documentId.trim() === "") {
      throw new Error("Invalid ID: ID is undefined, empty, or not a valid string");
    }

    await admin.firestore().collection(collectionName).doc(documentId).set(
      { taskId: taskId,
        token: deviceToken,
        description: description
       });

    console.log(`Created task ${taskId} for alarm ${id}`);

    await snap.ref.delete();
    //console.log(`Document with id ${id} is deleted from the schedule collection.`);
  } catch (error) {
    console.error("Error creating Cloud Task:", error);
  }
}

exports.sendNotificationTask = functions.https.onRequest(async (req, res) => {
  const authHeader = req.headers['authorization'] || '';
  const idToken = authHeader.startsWith('Bearer ') ? authHeader.split('Bearer ')[1] : null;
  //console.log(`idToken ${idToken}`);

  if (!idToken) {
    res.status(403).send('Unauthorized');
    return;
  }

  const decoded = jwt.decode(idToken);
  if (decoded.aud != "https://us-central1-family-team-e3e00.cloudfunctions.net/sendNotificationTask") {
    console.error("Unauthorized request:", error);
    res.status(403).send('Unauthorized');
    return;
  }

  const { description, deviceToken, id } = req.body;


  const message = {
    token: deviceToken,
    notification: {
      title: "Reminder",
      body: description,
    },
  };

  try {
    await admin.messaging().send(message);
    //console.log(`Notification sent for ${id} with description: ${description}`);
    res.status(200).send("Notification sent.");
  } catch (error) {
    console.error("Error sending notification:", error);
    res.status(500).send("Failed to send notification.");
  }
});

exports.onScheduleCreate = onDocumentCreated("schedule/{docId}", async (event) => {
  const snap = event.data;
  if (!snap) {
    console.log("No data associated with the event");
    return;
  }


  const data = snap.data();
  if (!data) {
    console.log("No data found in the document");
    return;
  }
  const { id, description, deviceToken, alarmTime } = data;

  createTask(id, description, deviceToken, alarmTime);
  

  return null;
});




exports.onTokenChanged = onDocumentCreated("token-changed/{docId}", async (event) => {
  

  const snap = event.data;
  if (!snap) {
    console.log("No data associated with the event");
    return;
  }


  const data = snap.data();
  if (!data) {
    console.log("No data found in the document");
    return;
  }
  const { token, remindersList } = data;
    
  if (!Array.isArray(remindersList) || remindersList.length === 0) {
      return;
  }

  
  try {
  
    const parent = client.queuePath(projectId, location, queue);

    
    const cancelTasksPromises = remindersList.map(async (taskId) => {
      const taskName = `${parent}/tasks/${taskId}`;
      try {
        await client.deleteTask({ name: taskName });
      } catch (error) {
        console.error(`Error delete task ID ${taskId}:`, error.message);
      }
    });

    
    await Promise.all(cancelTasksPromises);
    //console.log('All tasks from remindersList are done');
  } catch (error) {
    console.error('error deleting tasks:', error.message);
  }
});










exports.onScheduleDelete = onDocumentCreated("schedule-delete/{docId}", async (event) => {
  const snap = event.data;
  if (!snap) {
    console.log("No data associated with the event");
    return;
  }

  const data = snap.data();
  if (!data) {
    console.log("No data found in the document");
    return;
  }

  const { id } = data;

  if (!id) {
    console.error("No ID found in the deleted document");
    return;
  }

  console.log(`Processing delete request for ID: ${id}`);

  try {

    const documentIdRemindersId = String(id);

    if (!documentIdRemindersId || typeof documentIdRemindersId !== "string" || documentIdRemindersId.trim() === "") {
      throw new Error("Invalid ID: ID is undefined, empty, or not a valid string");
    }

    const remindersDoc = await admin.firestore().collection("reminders-in-queue").doc(documentIdRemindersId).get();

    if (!remindersDoc.exists) {
      //console.log(`No task found in reminders-in-queue for ID: ${id}`);

      await snap.ref.delete();
      //console.log(`Deleted processed document with ID: ${id} from schedule-delete`);
      return;
    }

    const { taskId } = remindersDoc.data();

    if (!taskId) {
      //console.error(`No taskId found for ID: ${id} in reminders-in-queue`);

      await snap.ref.delete();
      //console.log(`Deleted processed document with ID: ${id} from schedule-delete`);
      return;
    }

    //console.log(`Found taskId: ${taskId} for ID: ${id}`);

    const projectId = "family-team-e3e00";
    const location = "us-central1";
    const queue = "notifications-queue";


    const taskName = client.taskPath(projectId, location, queue, taskId);

    await client.deleteTask({ name: taskName });
    //console.log(`Task with ID: ${taskId} deleted successfully from queue: ${queue}`);

    await admin.firestore().collection("reminders-in-queue").doc(documentIdRemindersId).delete();
    //console.log(`Document with ID: ${id} deleted from reminders-in-queue`);

    await snap.ref.delete();
    //console.log(`Deleted processed document with ID: ${id} from schedule-delete`);
  } catch (error) {
    console.error("Error processing delete request:", error);
  }
});

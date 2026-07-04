import * as admin from "firebase-admin";
import {onDocumentCreated} from "firebase-functions/v2/firestore";

admin.initializeApp();

export const onNotificationCreated = onDocumentCreated(
  "notifications/{notificationId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const notification = snap.data();
    if (!notification) return;

    const userDoc = await admin.firestore()
      .collection("users")
      .doc(notification.userId)
      .get();

    const fcmToken = userDoc.data()?.fcmToken;
    if (!fcmToken) return;

    await admin.messaging().send({
      token: fcmToken,
      data: {
        notificationId: snap.id,
        type: notification.type ?? "",
        recipeId: notification.recipeId ?? "",
        triggerUserId: notification.triggerUserId ?? "",
        userId: notification.userId ?? "",
      },
      android: {priority: "high"},
    });
  }
);

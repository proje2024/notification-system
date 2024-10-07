import stopNotifications from "../StopNotification/StopNotification";

const ShowBrowserNotification = (message, eventId, stopNotification) => {
  if (!("Notification" in window)) {
    return;
  }

  if (Notification.permission === "granted") {
    const notification = new Notification(message, {
      body: "Bildirimi iptal etmek için tıklayınız",
    });

    notification.onclick = () => {
      stopNotifications(eventId, stopNotification); // stopNotification argüman olarak geçiliyor
    };
  } else if (Notification.permission !== "denied") {
    Notification.requestPermission().then((permission) => {
      if (permission === "granted") {
        const notification = new Notification(message, {
          body: "Bildirimi iptal etmek için tıklayınız",
        });

        notification.onclick = () => {
          stopNotifications(eventId, stopNotification); // stopNotification argüman olarak geçiliyor
        };
      }
    });
  }
};

export default ShowBrowserNotification;

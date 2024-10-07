import React, { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import NotificationSnackbar from "../NotificationSnackbar/NotificationSnackbar";
import showBrowserNotification from "../ShowBrowserNotification/ShowBrowserNotification";
import { useAuth } from "../../AuthContext";

const Notification = () => {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState("");
  const [eventId, setEventId] = useState(null);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isAuthenticated) return;

    const token = localStorage.getItem("token");
    if (!token) return;

    const socket = new SockJS(`/ws?token=${token}`);
    const stompClient = Stomp.over(socket);

    stompClient.connect(
      {},
      (frame) => {
        stompClient.subscribe("/user/topic/notifications", (message) => {
          if (message.body) {
            const notification = JSON.parse(message.body);

            const eventDate = new Date(notification.eventTime);
            const formattedEventTime = eventDate.toLocaleString("tr-TR", {
              year: "numeric",
              month: "long",
              day: "numeric",
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            });

            setMessage(
              `${notification.eventName} etkinliğinizin tarihi ${formattedEventTime} dır.`
            );
            setEventId(notification.id);
            setOpen(true);
            document.title = `(1) Yeni Bildirim`;

            if (document.hidden) {
              showBrowserNotification(
                `${notification.eventName} etkinliğinizin tarihi ${formattedEventTime} dır.`,
                notification.id
              );
            }
          }
        });
      },
      (error) => {
        console.error("Bildirim esnasında hata oluştu: " + error);
      }
    );

    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, [isAuthenticated]);

  const handleClose = () => {
    setOpen(false);
    document.title = "Bildirim Sistemi";
  };

  return (
    <NotificationSnackbar
      open={open}
      message={message}
      eventId={eventId}
      handleClose={handleClose}
    />
  );
};

export default Notification;

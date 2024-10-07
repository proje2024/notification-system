import React, { useState, useEffect } from "react";
import moment from "moment";
import NotificationsIcon from "@mui/icons-material/Notifications";
import NotificationsOffIcon from "@mui/icons-material/NotificationsOff";
import axios from "axios";
import { message } from "antd";
import { useNotification } from "../../NotificationContext";
import "./Agenda.css";

const Agenda = ({ events, onEventClick, onDateClick }) => {
  const { notifications, stopNotification, startNotification } =
    useNotification();

  const [eventNotifications, setEventNotifications] = useState({});

  useEffect(() => {
    const updatedNotifications = events.reduce((acc, event) => {
      acc[event.id] = notifications[event.id] ?? event.notificationEnabled;
      return acc;
    }, {});
    setEventNotifications(updatedNotifications);
  }, [notifications, events]);

  const toggleNotification = async (eventId, currentStatus) => {
    try {
      const newStatus = !currentStatus;
      setEventNotifications((prev) => ({
        ...prev,
        [eventId]: newStatus,
      }));

      await axios.post(`/api/events/change-notification-enable/${eventId}`, {
        notificationEnabled: newStatus,
      });

      if (newStatus) {
        startNotification(eventId);
        message.success("Bildirim aktif edildi");
      } else {
        stopNotification(eventId);
        message.success("Bildirim iptal edildi");
      }
    } catch (error) {
      setEventNotifications((prev) => ({
        ...prev,
        [eventId]: currentStatus,
      }));
      message.error("Bildirim durumu değiştirilirken hata oluştu");
    }
  };

  const groupedEvents = events.reduce((acc, event) => {
    const date = moment(event.eventTime).format("YYYY-MM-DD");
    if (!acc[date]) acc[date] = [];
    acc[date].push(event);
    return acc;
  }, {});

  const sortedDates = Object.keys(groupedEvents).sort(
    (a, b) => new Date(a) - new Date(b)
  );

  return (
    <div className="agenda-container">
      <div className="agenda-title">AJANDA</div>
      <div className="agenda-container-inner">
        <div className="agenda-event-container">
          {sortedDates && sortedDates.length > 0 ? (
            sortedDates.map((date) => (
              <div key={date} className="agenda-event-list">
                <div
                  className="agenda-date"
                  onClick={() => onDateClick(moment(date).toDate())}
                  title="Tarihe git"
                >
                  {moment(date).format("LL")}
                </div>
                {groupedEvents[date]
                  .sort((a, b) => new Date(a.eventTime) - new Date(b.eventTime))
                  .map((event) => (
                    <span
                      key={event.id}
                      className="agenda-event"
                      onClick={() => onEventClick(event)}
                    >
                      <span
                        className="agenda-notification-icon"
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleNotification(
                            event.id,
                            eventNotifications[event.id]
                          );
                        }}
                      >
                        {eventNotifications[event.id] ? (
                          <span title="Bildirimi kapat">
                            <NotificationsIcon style={{ color: "#FFD700" }} />
                          </span>
                        ) : (
                          <span title="Bildirimi aç">
                            <NotificationsOffIcon
                              style={{ color: "rgba(255, 140, 0, 0.68)" }}
                            />
                          </span>
                        )}
                      </span>
                      <span title="Düzenle">
                        {event.eventName} -{" "}
                        {moment(event.eventTime).format("HH:mm")}
                      </span>
                    </span>
                  ))}
              </div>
            ))
          ) : (
            <span className="agenda-non-content">
              Etkinliğiniz bulunmamaktadır.
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default Agenda;

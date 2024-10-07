import React, { useState, useEffect, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { Calendar as BigCalendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "moment/locale/tr";
import "react-big-calendar/lib/css/react-big-calendar.css";
import withDragAndDrop from "react-big-calendar/lib/addons/dragAndDrop";
import "react-big-calendar/lib/addons/dragAndDrop/styles.css";
import "./Calendar.css";
import axios from "axios";
import jwtDecode from "jwt-decode";
import EventModal from "../../components/EventModal/EventModal";
import EventCreateModal from "../../components/EventCreateModal/EventCreateModal";
import { utcToZonedTime } from "date-fns-tz";
import FilePresentIcon from "@mui/icons-material/FilePresent";
import { message } from "antd";
import { useNotification } from "../../NotificationContext";

import Agenda from "../../components/Agenda/Agenda";

moment.locale("tr");
const localizer = momentLocalizer(moment);
const DnDCalendar = withDragAndDrop(BigCalendar);

const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

const Calendar = () => {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null);
  const [viewDate, setViewDate] = useState(new Date());
  const currentDateTime = new Date();

  const [searchParams] = useSearchParams();
  const [cancelNotificationId, setCancelNotificationId] = useState(null);
  const { stopNotification, startNotification } = useNotification();
  const token = localStorage.getItem("token");
  const decodedToken = jwtDecode(token);
  const userId = decodedToken.sub;

  let clickTimeout = null;

  useEffect(() => {
    setCancelNotificationId(searchParams.get("cancelNotification"));
  }, [searchParams]);

  useEffect(() => {
    const handleUrlChange = () => {
      const params = new URLSearchParams(window.location.search);
      const newCancelNotificationId = params.get("cancelNotification");
      if (newCancelNotificationId) {
        setCancelNotificationId(newCancelNotificationId);
      }
    };

    window.addEventListener("popstate", handleUrlChange);

    handleUrlChange();

    return () => {
      window.removeEventListener("popstate", handleUrlChange);
    };
  }, []);

  const fetchEvents = useCallback(async () => {
    try {
      const response = await axios.get(`/api/events/user`);
      const eventData = response.data.map((event) => ({
        ...event,
        start: utcToZonedTime(new Date(event.eventTime), event.timeZone),
        end: utcToZonedTime(new Date(event.eventTime), event.timeZone),
        title: event.eventName,
        hasFile: event.fileName !== null && event.fileName !== "",
      }));
      setEvents(eventData);
    } catch (error) {
      console.error("Etkinlikler çekilirken hata oluştu", error);
      message.error("Etkinlikler çekilirken hata oluştu");
    }
  }, [userId]);

  const stopNotificationHandler = async () => {
    if (cancelNotificationId) {
      try {
        await axios.post(
          `/api/events/${cancelNotificationId}/stop-notifications`
        );
        message.success("Bildirim başarıyla iptal edildi");

        setEvents((prevEvents) =>
          prevEvents.map((event) =>
            event.id === cancelNotificationId
              ? { ...event, notificationEnabled: false }
              : event
          )
        );
        stopNotification(cancelNotificationId);
        console.log("calender sayfası fetch event oldu.");
        window.history.replaceState(null, "", "/calendar");
      } catch (error) {
        message.error("Bildirim iptal edilirken hata oluştu");
      }
    }
  };

  useEffect(() => {
    stopNotificationHandler();
    fetchEvents();
  }, [cancelNotificationId, fetchEvents]);

  const handleSelectEvent = (event) => {
    if (event) {
      setSelectedEvent(event);
      setModalOpen(true);
    }
  };

  const handleSelectSlot = (slotInfo) => {
    if (clickTimeout) {
      clearTimeout(clickTimeout);
      clickTimeout = null;
      handleDoubleClickSlot(slotInfo);
    } else {
      clickTimeout = setTimeout(() => {
        clickTimeout = null;
      }, 300);
    }
  };

  const handleDoubleClickSlot = ({ start }) => {
    setSelectedDate(start);
    setCreateModalOpen(true);
  };

  const handleDateClick = (date) => {
    setViewDate(date);
  };

  const moveEvent = async ({ event, start }) => {
    const participants = event.participants || [];
    const isFromUser =
      participants.length === 0 ||
      participants.some((participant) => participant.fromUserId === userId);

    if (!isFromUser) {
      message.error("Katılımcılar etkinlik güncelleyemez.");
      return;
    }

    const updatedEvent = {
      ...event,
      eventTime: new Date(start).toISOString(),
    };

    if (new Date(start).toISOString() < currentDateTime.toISOString()) {
      message.error("Etkinlik tarihi bugünden önce olamaz");
    }

    if (
      new Date(start).toISOString() <
      new Date(event.notificationStartTime).toISOString()
    ) {
      message.error(
        "Bildirim başlangıç tarihi etkinlik tarihinden sonra olamaz"
      );
    }
    const participantsEmails =
      participants && participants.length > 0
        ? participants.map((p) => p.toUserEmail)
        : [];

    const eventJson = {
      id: event.id,
      eventName: event.eventName,
      eventTime: updatedEvent.eventTime,
      notificationFrequency: event.notificationFrequency,
      notificationInterval: event.notificationInterval,
      notificationStartTime: new Date(
        event.notificationStartTime
      ).toISOString(),
      notificationEnabled: event.notificationEnabled,
      timeZone: userTimeZone,
      invitees: participantsEmails,
    };

    const formData = new FormData();
    formData.append("event", JSON.stringify(eventJson));

    try {
      const response = await axios.put(`/api/events/${event.id}`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      const updatedEvents = events.map((e) =>
        e.id === event.id
          ? {
              ...e,
              ...response.data,
              start: utcToZonedTime(
                new Date(response.data.eventTime),
                response.data.timeZone
              ),
              end: utcToZonedTime(
                new Date(response.data.eventTime),
                response.data.timeZone
              ),
              title: response.data.eventName,
            }
          : e
      );
      setEvents(updatedEvents);
      message.success("Etkinlik güncellendi");
    } catch (error) {
      console.error("Etkinlik güncellenirken hata oluştu", error);
      message.error("Etkinlik güncellenirken hata oluştu");
    }
  };

  const EventComponent = ({ event }) => (
    <>
      {event.hasFile && (
        <FilePresentIcon style={{ marginRight: 5, fontSize: "small" }} />
      )}{" "}
      <span title="Düzenle">{event.title}</span>
    </>
  );

  return (
    <div className="calendar-container">
      <Agenda
        events={events}
        onEventClick={handleSelectEvent}
        onDateClick={handleDateClick}
      />

      <div style={{ flex: 1 }}>
        <DnDCalendar
          localizer={localizer}
          events={events}
          startAccessor="start"
          endAccessor="end"
          date={viewDate}
          onNavigate={(date) => setViewDate(date)}
          style={{ height: 700, width: "100%" }}
          selectable
          onEventDrop={moveEvent}
          onSelectEvent={handleSelectEvent}
          onSelectSlot={handleSelectSlot}
          resizable
          components={{
            event: EventComponent,
          }}
          step={60}
          timeslots={1}
          messages={{
            next: "İleri",
            previous: "Geri",
            today: "Bugün",
            month: "Ay",
            week: "Hafta",
            day: "Gün",
            agenda: "Ajanda",
            date: "Tarih",
            time: "Zaman",
            event: "Etkinlik",
            allDay: "Tüm Gün",
            noEventsInRange: "Bu aralıkta etkinlik yok",
            showMore: (total) => `+ ${total} daha fazla`,
          }}
        />
      </div>

      {selectedEvent && (
        <EventModal
          event={selectedEvent}
          open={modalOpen}
          onClose={() => {
            setModalOpen(false);
            setSelectedEvent(null);
            fetchEvents();
          }}
          setEvents={setEvents}
          events={events}
        />
      )}
      {createModalOpen && (
        <EventCreateModal
          open={createModalOpen}
          onClose={() => {
            setCreateModalOpen(false);
            fetchEvents();
          }}
          setEvents={setEvents}
          startDate={selectedDate}
        />
      )}
    </div>
  );
};

export default Calendar;

import React, { useState, useEffect } from "react";
import axios from "axios";
import EventList from "../../components/EventList/EventList";
import EventForm from "../../components/EventForm/EventForm";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import { message } from "antd";
import "./Home.css";

const Home = () => {
  const [events, setEvents] = useState([]);
  const [showForm, setShowForm] = useState(true);
  const [showList, setShowList] = useState(false);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await axios.get(`/api/events/user`);
        setEvents(response.data);
      } catch (error) {
        console.error("Etkinlikler çekilirken hata oluştu", error);
        message.error("Etkinlikler çekilirken hata oluştu");
      }
    };

    fetchEvents();
  }, []);

  const addEvent = (newEvent) => {
    setEvents((prevEvents) => [...prevEvents, newEvent]);
  };

  const updateEvent = (eventId, updatedEvent) => {
    setEvents(
      events.map((event) => (event.id === eventId ? updatedEvent : event))
    );
  };

  const deleteEvent = async (eventId) => {
    try {
      await axios.delete(`/api/events/${eventId}`);
      setEvents(events.filter((event) => event.id !== eventId));
      message.success("Etkinlik silindi");
    } catch (error) {
      console.error("Etkinlik silinirken hata oluştu", error);
      message.error("Etkinlik silinirken hata oluştu");
    }
  };

  return (
    <div className="home-container">
      <h1>Etkinlikler</h1>
      <div className="home-content">
        <div className="home-left">
          <button
            className="toggle-button"
            onClick={() => setShowForm(!showForm)}
          >
            Etkinlik Oluştur
            {showForm ? <RemoveIcon /> : <AddIcon />}
          </button>
          {showForm && (
            <div className="home-event-form-container">
              <EventForm addEvent={addEvent} />
            </div>
          )}
        </div>
        <div className="home-right">
          <button
            className="toggle-button"
            onClick={() => setShowList(!showList)}
          >
            Etkinlikleri Görüntüle
            {showList ? <RemoveIcon /> : <AddIcon />}
          </button>
          {showList && (
            <EventList
              events={events}
              updateEvent={updateEvent}
              deleteEvent={deleteEvent}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default Home;

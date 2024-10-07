import React from "react";
import EventItem from "../EventItem/EventItem";
import "./EventList.css";

const EventList = ({ events, updateEvent, deleteEvent }) => {
  return (
    <div className="event-list">
      {events.map((event) => (
        <EventItem
          key={event.id}
          event={event}
          updateEvent={updateEvent}
          deleteEvent={deleteEvent}
        />
      ))}
    </div>
  );
};

export default EventList;

import React from "react";
import EventModalCreate from "../EventModalCreate/EventModalCreate";

const EventModal = ({ event, open, onClose, setEvents, events }) => {
  return (
    <EventModalCreate
      open={open}
      onClose={onClose}
      setEvents={setEvents}
      events={events}
      event={event}
      isCreateMode={false}
    />
  );
};

export default EventModal;

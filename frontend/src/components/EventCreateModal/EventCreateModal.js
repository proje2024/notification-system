import React from "react";
import EventModalCreate from "../EventModalCreate/EventModalCreate";

const EventCreateModal = ({ open, onClose, setEvents, startDate }) => {
  return (
    <EventModalCreate
      open={open}
      onClose={onClose}
      setEvents={setEvents}
      startDate={startDate}
      isCreateMode={true}
    />
  );
};

export default EventCreateModal;

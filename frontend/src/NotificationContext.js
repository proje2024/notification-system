import React, { createContext, useContext, useState } from "react";

const NotificationContext = createContext();

export const useNotification = () => useContext(NotificationContext);

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState({});
  const stopNotification = (eventId) => {
    setNotifications((prev) => ({
      ...prev,
      [eventId]: false,
    }));
  };

  const startNotification = (eventId) => {
    setNotifications((prev) => ({
      ...prev,
      [eventId]: true,
    }));
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        stopNotification,
        startNotification,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

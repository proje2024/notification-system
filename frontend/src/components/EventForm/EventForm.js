import React, { useRef, useState } from "react";
import "./EventForm.css";
import { message } from "antd";
import EventFormCreate from "../EventFormCreate/EventFormCreate";
import axios from "axios";

const EventForm = ({ addEvent }) => {
  const [eventName, setEventName] = useState("");
  const [eventTime, setEventTime] = useState(null);
  const [notificationFrequency, setNotificationFrequency] = useState("");
  const [notificationInterval, setNotificationInterval] = useState("");
  const [notificationStartTime, setNotificationStartTime] = useState(null);
  const [file, setFile] = useState(null);
  const [errors, setErrors] = useState({});
  const [invitees, setInvitees] = useState([]);
  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const fileInputRef = useRef(null);
  const currentDateTime = new Date();

  const isErrorsEmpty = (errors) => {
    return Object.values(errors).every((value) => value === "");
  };

  const handleSubmit = async () => {
    const newErrors = {};
    let requiredError = false;

    if (!eventName) {
      newErrors.eventName = "*";
      requiredError = true;
    }
    if (!eventTime) {
      newErrors.eventTime = "*";
      requiredError = true;
    }
    if (!notificationFrequency) {
      newErrors.notificationFrequency = "*";
      requiredError = true;
    }
    if (!notificationInterval) {
      newErrors.notificationInterval = "*";
      requiredError = true;
    }
    if (!notificationStartTime) {
      newErrors.notificationStartTime = "*";
      requiredError = true;
    }

    if (requiredError) {
      message.error("Kırmızı alanlar boş olamaz");
    }

    if (notificationFrequency && notificationFrequency < 1) {
      newErrors.notificationFrequency = "*";

      message.error("Bildirim sıklığı 1'den küçük olamaz");
    }

    if (new Date(eventTime).toISOString() < currentDateTime.toISOString()) {
      newErrors.eventTimeError = "*";

      message.error("Etkinlik tarihi bugünden önce olamaz");
    }

    if (
      new Date(eventTime).toISOString() <
      new Date(notificationStartTime).toISOString()
    ) {
      newErrors.notificationTimeError = "*";

      message.error(
        "Bildirim başlangıç tarihi etkinlik tarihinden sonra olamaz"
      );
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        ...newErrors,
      }));
      return;
    }

    if (!isErrorsEmpty(errors)) {
      return;
    }

    const event = {
      eventName,
      eventTime: new Date(eventTime).toISOString(),
      notificationFrequency,
      notificationInterval,
      notificationEnabled: true,
      notificationStartTime: new Date(notificationStartTime).toISOString(),
      timeZone: userTimeZone,
      invitees: invitees,
    };

    const formData = new FormData();
    formData.append("event", JSON.stringify(event));
    if (file) {
      formData.append("file", file);
    }

    try {
      const response = await axios.post(`/api/events`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      addEvent(response.data);
      setEventName("");
      setEventTime(null);
      setNotificationFrequency("");
      setNotificationInterval("");
      setNotificationStartTime(null);
      setFile(null);
      setInvitees([]);
      setErrors({});
      message.success("Etkinlik oluşturuldu");
    } catch (error) {
      if (error.response && error.response.status === 413) {
        setErrors((prev) => ({
          ...prev,
          file: "*",
        }));

        message.error(
          "Dosya boyutu çok büyük, lütfen daha küçük bir dosya seçin"
        );
      } else {
        setErrors((prev) => ({
          ...prev,
          file: "*",
        }));
        message.error("Dosya yüklenirken hata oluştu, lütfen tekrar deneyin");
      }
    }

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
      setFile(null);
    }
  };

  const handleInputFileDelete = (e) => {
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
      setErrors((prev) => ({ ...prev, file: "" }));
    }
    setFile(null);
  };

  return (
    <>
      <EventFormCreate
        handleSubmit={handleSubmit}
        handleInputFileDelete={handleInputFileDelete}
        errors={errors}
        setErrors={setErrors}
        eventName={eventName}
        setEventName={setEventName}
        eventTime={eventTime}
        setEventTime={setEventTime}
        notificationFrequency={notificationFrequency}
        setNotificationFrequency={setNotificationFrequency}
        notificationInterval={notificationInterval}
        setNotificationInterval={setNotificationInterval}
        notificationStartTime={notificationStartTime}
        setNotificationStartTime={setNotificationStartTime}
        file={file}
        setFile={setFile}
        fileInputRef={fileInputRef}
        invitees={invitees}
        setInvitees={setInvitees}
      />
    </>
  );
};

export default EventForm;

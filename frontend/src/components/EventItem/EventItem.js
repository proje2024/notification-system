import React, { useState, useEffect, useRef } from "react";
import "./EventItem.css";
import { FaEdit, FaTrash } from "react-icons/fa";
import EventFormCreate from "../EventFormCreate/EventFormCreate";
import { format, utcToZonedTime } from "date-fns-tz";
import axios from "axios";
import { message } from "antd";
import jwtDecode from "jwt-decode";

const EventItem = ({ event, updateEvent, deleteEvent }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [eventName, setEventName] = useState(event.eventName);
  const [file, setFile] = useState(null);
  const [fileName, setFileName] = useState(event.fileName);
  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const [eventTime, setEventTime] = useState(
    utcToZonedTime(new Date(event.eventTime), event.timeZone)
  );
  const [notificationFrequency, setNotificationFrequency] = useState(
    event.notificationFrequency
  );
  const [notificationInterval, setNotificationInterval] = useState(
    event.notificationInterval
  );
  const [notificationStartTime, setNotificationStartTime] = useState(
    utcToZonedTime(new Date(event.notificationStartTime), event.timeZone)
  );
  const [errors, setErrors] = useState({});
  const [invitees, setInvitees] = useState(event.invitees || []);
  const fileInputRef = useRef(null);
  const [fileDeleted, setFileDeleted] = useState(false);
  const currentDateTime = new Date();

  const token = localStorage.getItem("token");
  const decodedToken = jwtDecode(token);
  const userId = decodedToken.sub; // userId'yi JWT'den alıyoruz

  // Null kontrolü ekleniyor
  const participants = event.participants || [];

  const isFromUser =
    participants.length === 0 ||
    participants.some((participant) => participant.fromUserId === userId);

  useEffect(() => {
    setEventName(event.eventName);
    setNotificationFrequency(event.notificationFrequency);
    setNotificationInterval(event.notificationInterval);
    setNotificationStartTime(
      utcToZonedTime(new Date(event.notificationStartTime), event.timeZone)
    );
    setEventTime(utcToZonedTime(new Date(event.eventTime), event.timeZone));
    setFileName(event.fileName);
    const participantsEmails = participants.map(
      (participant) => participant.toUserEmail
    );
    setInvitees(participantsEmails);
    setFileDeleted(false);
  }, [event]);

  const isErrorsEmpty = (errors) => {
    return Object.values(errors).every((value) => value === "");
  };
  const handleUpdate = async () => {
    if (!isFromUser) {
      return;
    }

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

    const eventJson = {
      eventName,
      eventTime: new Date(eventTime).toISOString(),
      notificationFrequency,
      notificationInterval,
      notificationEnabled: event.notificationEnabled,
      notificationStartTime: new Date(notificationStartTime).toISOString(),
      timeZone: userTimeZone,
      fileDeleted: fileDeleted,
      invitees: invitees,
    };

    const formData = new FormData();
    formData.append("event", JSON.stringify(eventJson));
    if (file) {
      formData.append("file", file);
    }

    try {
      const response = await axios.put(`/api/events/${event.id}`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      updateEvent(event.id, response.data);
      setIsEditing(false);
      message.success("Etkinlik güncellendi");
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
        message.error("Dosya yüklenirken hata oluştu, lütfen tekrar deneyin");
        setErrors((prev) => ({
          ...prev,
          file: "*",
        }));
      }
    }
  };

  const translateInterval = (interval) => {
    switch (interval) {
      case "minutes":
        return "Dakika";
      case "hours":
        return "Saat";
      case "days":
        return "Gün";
      case "weeks":
        return "Hafta";
      case "months":
        return "Ay";
      case "years":
        return "Yıl";
      default:
        return interval;
    }
  };

  const truncateFileName = (fileName, length) => {
    if (fileName.length > length) {
      return `${fileName.slice(0, length / 2)}...${fileName.slice(
        -length / 2
      )}`;
    }
    return fileName;
  };

  const handleInputFileDelete = (e) => {
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
      setErrors((prev) => ({ ...prev, file: "" }));
    }
    setFile(null);
  };

  const handleFileDelete = () => {
    setFileName("");
    setFileDeleted(true);
    setFile(null);
  };

  const updateFileName = (newFileName) => {
    setFileName(newFileName);
  };

  const toggleEditMode = () => {
    if (isEditing) {
      const participantsEmails = participants.map(
        (participant) => participant.toUserEmail
      );
      setInvitees(participantsEmails);

      setFileName(event.fileName);
      setFile(null);
    }
    setIsEditing(!isEditing);
  };

  return (
    <>
      {isEditing ? (
        <EventFormCreate
          handleSubmit={handleUpdate}
          handleInputFileDelete={handleInputFileDelete}
          handleFileDelete={handleFileDelete}
          truncateFileName={truncateFileName}
          updateFileName={updateFileName}
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
          fileName={fileName}
          showIcons={isFromUser}
          toggleEditMode={toggleEditMode}
          invitees={invitees}
          setInvitees={setInvitees}
          deleteEvent={deleteEvent}
        />
      ) : (
        <div className="event-item">
          <div className="event-item-view">
            <div className="event-item-icons">
              {isFromUser && (
                <FaEdit
                  onClick={toggleEditMode}
                  className="event-item-icons-edit"
                />
              )}
              <FaTrash
                onClick={() => deleteEvent(event.id, userId)}
                className="event-item-icons-trash"
              />
            </div>
            <div className="event-item-input-group">
              <p>
                <strong className="event-item-title">
                  Etkinlik İsmi<p>:</p>
                </strong>
                {event.eventName}
              </p>
            </div>

            <div className="event-item-input-group">
              <p>
                <strong className="event-item-title">
                  Etkinlik Tarihi<p>:</p>
                </strong>
                {format(
                  utcToZonedTime(new Date(event.eventTime), event.timeZone),
                  "dd/MM/yyyy HH:mm"
                )}
              </p>
            </div>
            <div className="event-item-input-group">
              <p>
                <strong className="event-item-title">
                  Bildirim Sıklığı<p>:</p>
                </strong>
                {event.notificationFrequency}
                {translateInterval(event.notificationInterval)}
              </p>
            </div>
            <div className="event-item-input-group">
              <p>
                <strong className="event-item-title">
                  Bildirim Başlangıç Tarihi<p>:</p>
                </strong>
                {format(
                  utcToZonedTime(
                    new Date(event.notificationStartTime),
                    event.timeZone
                  ),
                  "dd/MM/yyyy HH:mm"
                )}
              </p>
            </div>
            {invitees.length > 0 && (
              <div className="event-item-input-group">
                <p>
                  <strong className="event-item-title">
                    Davetliler<p>:</p>
                  </strong>
                  <ul className="event-item-invitees-list">
                    {invitees.map((invitee) => (
                      <li key={invitee}>{invitee}</li>
                    ))}
                  </ul>
                </p>
              </div>
            )}
            {fileName && (
              <div className="event-item-input-group">
                <p>
                  <strong className="event-item-title">
                    Dosya<p>:</p>
                  </strong>
                  {truncateFileName(fileName, 30)}
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </>
  );
};

export default EventItem;

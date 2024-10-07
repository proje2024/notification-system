import React, { useState, useEffect, useRef } from "react";
import { Modal, TextField, Button, MenuItem } from "@mui/material";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { Close as CloseIcon } from "@mui/icons-material";
import trLocale from "date-fns/locale/tr";
import { utcToZonedTime } from "date-fns-tz";
import axios from "axios";
import { message } from "antd";
import jwtDecode from "jwt-decode";
import "./EventModalCreate.css";

const EventModalCreate = ({
  open,
  onClose,
  setEvents,
  events,
  event,
  isCreateMode,
  startDate,
}) => {
  const [eventName, setEventName] = useState(event ? event.eventName : "");
  const [notificationFrequency, setNotificationFrequency] = useState(
    event ? event.notificationFrequency : ""
  );
  const [notificationInterval, setNotificationInterval] = useState(
    event ? event.notificationInterval : ""
  );
  const [notificationStartTime, setNotificationStartTime] = useState(
    event
      ? utcToZonedTime(new Date(event.notificationStartTime), event.timeZone)
      : ""
  );
  const [eventTime, setEventTime] = useState(
    event
      ? utcToZonedTime(new Date(event.eventTime), event.timeZone)
      : startDate
  );
  const [file, setFile] = useState(null);
  const [fileName, setFileName] = useState(event ? event.fileName : "");
  const [errors, setErrors] = useState({});
  const [fileDeleted, setFileDeleted] = useState(false);
  const [invitees, setInvitees] = useState(event ? event.invitees : []);
  const [inviteeEmail, setInviteeEmail] = useState("");
  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const fileInputRef = useRef(null);
  const currentDateTime = new Date();

  const token = localStorage.getItem("token");
  const decodedToken = jwtDecode(token);
  const userId = decodedToken.sub;
  const participants = event?.participants || [];
  const isFromUser =
    participants.length === 0 ||
    participants.some((participant) => participant.fromUserId === userId);
  useEffect(() => {
    if (event) {
      setEventName(event.eventName);
      setNotificationFrequency(event.notificationFrequency);
      setNotificationInterval(event.notificationInterval);
      setNotificationStartTime(
        utcToZonedTime(new Date(event.notificationStartTime), event.timeZone)
      );
      setEventTime(utcToZonedTime(new Date(event.eventTime), event.timeZone));
      setFileName(event.fileName);
      setFileDeleted(false);
      const participantsEmails = event.participants
        ? event.participants.map((participant) => participant.toUserEmail)
        : [];
      setInvitees(participantsEmails);
    }
  }, [event]);

  const isErrorsEmpty = (errors) => {
    return Object.values(errors).every((value) => value === "");
  };

  const handleSave = async () => {
    const newErrors = {};

    if (!eventName) newErrors.eventName = "*Bu alan gereklidir";
    if (!eventTime) newErrors.eventTime = "*Bu alan gereklidir";
    if (!notificationFrequency)
      newErrors.notificationFrequency = "*Bu alan gereklidir";
    if (!notificationInterval)
      newErrors.notificationInterval = "*Bu alan gereklidir";
    if (!notificationStartTime)
      newErrors.notificationStartTime = "*Bu alan gereklidir";

    if (eventTime && notificationStartTime) {
      if (
        new Date(eventTime).toISOString() <
        new Date(notificationStartTime).toISOString()
      ) {
        newErrors.notificationStartTime = "* Etkinlik tarihinden sonra olamaz";
      }
      if (new Date(eventTime).toISOString() < currentDateTime.toISOString()) {
        newErrors.eventTime = "* Bugünden önce olamaz";
      }
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
      notificationEnabled: isCreateMode ? true : event.notificationEnabled,
      notificationStartTime: new Date(notificationStartTime).toISOString(),
      timeZone: userTimeZone,
      fileDeleted: fileDeleted,
      invitees: invitees.length > 0 ? invitees : [],
    };

    const formData = new FormData();
    formData.append("event", JSON.stringify(eventJson));
    if (file) {
      formData.append("file", file);
    }

    try {
      if (isCreateMode) {
        const response = await axios.post(`/api/events`, formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });

        const createdEvent = {
          ...response.data,
          title: response.data.eventName,
          start: utcToZonedTime(
            new Date(response.data.eventTime),
            response.data.timeZone
          ),
          end: utcToZonedTime(
            new Date(response.data.eventTime),
            response.data.timeZone
          ),
        };

        setEvents((prevEvents) => [...prevEvents, createdEvent]);
        onClose();
        message.success("Etkinlik oluşturuldu");
      } else {
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
                title: response.data.eventName,
                start: utcToZonedTime(
                  new Date(response.data.eventTime),
                  response.data.timeZone
                ),
                end: utcToZonedTime(
                  new Date(response.data.eventTime),
                  response.data.timeZone
                ),
              }
            : e
        );
        setEvents(updatedEvents);
        onClose();
        message.success("Etkinlik güncellendi");
      }
    } catch (error) {
      if (error.response && error.response.status === 413) {
        console.error("Dosya boyutu çok büyük:", error);
        setErrors((prev) => ({
          ...prev,
          file: "Dosya boyutu çok büyük, lütfen daha küçük bir dosya seçin",
        }));
      } else {
        console.error("Dosya yüklenirken hata oluştu:", error);
        setErrors((prev) => ({
          ...prev,
          file: "Etkinlik oluşturulamadı, lütfen tekrar deneyin",
        }));
      }
    }
  };

  const handleAddInvitee = async () => {
    if (inviteeEmail && !invitees.includes(inviteeEmail)) {
      try {
        const response = await axios.get(`api/events/email/${inviteeEmail}`);
        if (response.data) {
          setInvitees([...invitees, inviteeEmail]);
          setInviteeEmail("");
          setErrors((prev) => ({ ...prev, inviteeEmail: "" }));
        } else {
          setErrors((prev) => ({
            ...prev,
            inviteeEmail: "Bu e-posta adresine ait kullanıcı bulunamadı",
          }));
        }
      } catch (error) {
        setErrors((prev) => ({
          ...prev,
          inviteeEmail: "Bu e-posta adresine ait kullanıcı bulunamadı",
        }));
      }
    }
  };

  const handleSubmit = () => {
    if (inviteeEmail) {
      setErrors((prev) => ({
        ...prev,
        inviteeEmail:
          "Lütfen e-posta alanındaki davetliyi ekleyin veya alanı boşaltın",
      }));
    } else {
      handleSave();
    }
  };

  const handleRemoveInvitee = (emailToRemove) => {
    setInvitees(invitees.filter((invitee) => invitee !== emailToRemove));
  };

  const handleInputFileDelete = (e) => {
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
      setErrors((prev) => ({ ...prev, file: "" }));
    }
    setFile(null);
  };

  const handleFileChange = (e) => {
    setErrors((prev) => ({ ...prev, file: "" }));
    const selectedFile = e.target.files[0];
    setFile(selectedFile);
    setFileName("");
    handleFileUpload(selectedFile);
  };

  const handleFileDelete = () => {
    setFileName("");
    setFileDeleted(true);
    setFile(null);
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`/api/events/${event.id}`);
      setEvents(events.filter((e) => e.id !== event.id));
      onClose();
      message.success("Etkinlik silindi");
    } catch (error) {
      console.error("Etkinlik silinirken hata oluştu", error);
    }
  };

  const truncateFileName = (fileName) => {
    if (fileName.length > 15) {
      return `${fileName.slice(0, 5)}...${fileName.slice(-7)}`;
    }
    return fileName;
  };

  const handleFileUpload = async (file) => {
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axios.post("/api/ocr/upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      const data = response.data;
      if (data.eventName) {
        setEventName(data.eventName);
      }
      if (data.eventTime) {
        const formattedEventTime = data.eventTime.replace(
          /(\d{2})\/(\d{2})\/(\d{4})/,
          "$3-$2-$1"
        );
        setEventTime(new Date(formattedEventTime));
      }
      if (data.notificationFrequency) {
        setNotificationFrequency(Number(data.notificationFrequency));
      }
      if (data.notificationInterval) {
        const intervalMap = {
          Dakika: "minutes",
          Saat: "hours",
          Gün: "days",
          Hafta: "weeks",
          Ay: "months",
          Yıl: "years",
        };
        const interval = intervalMap[data.notificationInterval];
        setNotificationInterval(interval);
      }
      if (data.notificationStartTime) {
        const formattedStartTime = data.notificationStartTime.replace(
          /(\d{2})\/(\d{2})\/(\d{4})/,
          "$3-$2-$1"
        );
        setNotificationStartTime(new Date(formattedStartTime));
      }
    } catch (error) {
      if (error.response && error.response.status === 413) {
        console.error("Dosya boyutu çok büyük:", error);
        setErrors((prev) => ({
          ...prev,
          file: "Dosya boyutu çok büyük, lütfen daha küçük bir dosya seçin",
        }));
      } else {
        console.error("Dosya yüklenirken hata oluştu:", error);
        setErrors((prev) => ({
          ...prev,
          file: "Dosya yüklenirken hata oluştu, lütfen tekrar deneyin",
        }));
      }
    }
  };

  return (
    <Modal open={open} onClose={onClose}>
      <div className="event-modal-create">
        <h2>{isCreateMode ? "Yeni Etkinlik Oluştur" : "Etkinlik Düzenle"}</h2>
        <TextField
          label="Etkinlik İsmi"
          value={eventName}
          onChange={(e) => {
            setEventName(e.target.value);
            setErrors((prev) => ({ ...prev, eventName: "" }));
          }}
          fullWidth
          error={!!errors.eventName}
          helperText={errors.eventName}
          disabled={!isFromUser}
        />
        <LocalizationProvider dateAdapter={AdapterDateFns} locale={trLocale}>
          <DateTimePicker
            label="Etkinlik Tarihi"
            value={eventTime}
            onChange={(newValue) => {
              setEventTime(new Date(newValue));
              setErrors((prev) => ({ ...prev, eventTime: "" }));
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                fullWidth
                error={!!errors.eventTime}
                helperText={errors.eventTime}
                disabled={!isFromUser}
              />
            )}
            readOnly={!isFromUser}
            ampm={false}
          />
        </LocalizationProvider>
        <TextField
          label="Bildirim Sıklığı"
          type="number"
          value={notificationFrequency}
          onChange={(e) => {
            setNotificationFrequency(e.target.value);
            setErrors((prev) => ({ ...prev, notificationFrequency: "" }));
          }}
          fullWidth
          inputProps={{ min: 1 }}
          error={!!errors.notificationFrequency}
          helperText={errors.notificationFrequency}
          disabled={!isFromUser}
        />
        <TextField
          label="Bildirim Aralığı"
          select
          value={notificationInterval}
          onChange={(e) => {
            setNotificationInterval(e.target.value);
            setErrors((prev) => ({ ...prev, notificationInterval: "" }));
          }}
          SelectProps={{ renderValue: (value) => (value !== "" ? value : " ") }}
          fullWidth
          error={!!errors.notificationInterval}
          helperText={errors.notificationInterval}
          disabled={!isFromUser}
        >
          <MenuItem value="" disabled>
            Seç
          </MenuItem>
          <MenuItem value="minutes">Dakika</MenuItem>
          <MenuItem value="hours">Saat</MenuItem>
          <MenuItem value="days">Gün</MenuItem>
          <MenuItem value="weeks">Hafta</MenuItem>
          <MenuItem value="months">Ay</MenuItem>
          <MenuItem value="years">Yıl</MenuItem>
        </TextField>
        <LocalizationProvider dateAdapter={AdapterDateFns} locale={trLocale}>
          <DateTimePicker
            label="Bildirim Başlangıç Tarihi"
            value={notificationStartTime}
            onChange={(newValue) => {
              setNotificationStartTime(new Date(newValue));
              setErrors((prev) => ({ ...prev, notificationStartTime: "" }));
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                fullWidth
                error={!!errors.notificationStartTime}
                helperText={errors.notificationStartTime}
                disabled={!isFromUser}
              />
            )}
            readOnly={!isFromUser}
            ampm={false}
          />
        </LocalizationProvider>
        <TextField
          variant="standard"
          label="Davetli E-posta"
          value={inviteeEmail}
          fullWidth
          onChange={(e) => {
            setInviteeEmail(e.target.value);
            setErrors((prev) => ({ ...prev, inviteeEmail: "" }));
          }}
          onKeyPress={(e) => {
            if (e.key === "Enter") {
              e.preventDefault();
              handleAddInvitee();
            }
          }}
          error={!!errors.inviteeEmail}
          helperText={errors.inviteeEmail}
          disabled={!isFromUser}
        />
        {invitees && invitees.length > 0 && (
          <ul className="event-modal-create-invitees-list">
            {invitees.map((invitee) => (
              <li key={invitee}>
                {invitee}
                {isFromUser && (
                  <button
                    className="event-modal-create-close-icon-button"
                    onClick={() => handleRemoveInvitee(invitee)}
                  >
                    <CloseIcon className="event-modal-create-close-icon" />
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
        <div className="event-modal-create-file-upload">
          <strong className="event-modal-create-title">Dosya:</strong>
          {fileName && (
            <div className="event-modal-create-input-file">
              <span>{truncateFileName(fileName)}</span>
              {isFromUser && (
                <button
                  className="event-modal-create-close-icon-button"
                  onClick={handleFileDelete}
                >
                  <CloseIcon className="event-modal-create-close-icon" />
                </button>
              )}
            </div>
          )}
          {isFromUser && (
            <input type="file" ref={fileInputRef} onChange={handleFileChange} />
          )}
          {file && (
            <button
              className="event-modal-create-input-close-button"
              onClick={handleInputFileDelete}
            >
              <CloseIcon className="event-modal-create-input-close-icon" />
            </button>
          )}
        </div>
        {!!errors.file && (
          <span className="event-modal-create-error-message">
            {errors.file}
          </span>
        )}
        <div className="event-modal-create-buttons">
          {isCreateMode ? (
            <Button onClick={handleSubmit} sx={{ color: "#e8eaed" }}>
              Oluştur
            </Button>
          ) : (
            isFromUser && (
              <Button onClick={handleSubmit} sx={{ color: "#e8eaed" }}>
                Güncelle
              </Button>
            )
          )}
          {!isCreateMode && (
            <Button onClick={handleDelete} sx={{ color: "#e8eaed" }}>
              Sil
            </Button>
          )}
        </div>
      </div>
    </Modal>
  );
};

export default EventModalCreate;

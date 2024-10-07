import React, { useState, useEffect } from "react";
import axios from "axios";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import TextField from "@mui/material/TextField";
import trLocale from "date-fns/locale/tr";
import { Close as CloseIcon } from "@mui/icons-material";
import { FaEdit, FaTrash } from "react-icons/fa";
import "./EventFormCreate.css";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import { message } from "antd";

const EventFormCreate = ({
  handleSubmit,
  handleInputFileDelete,
  handleFileDelete,
  truncateFileName,
  updateFileName,
  errors,
  setErrors,
  eventName,
  setEventName,
  eventTime,
  setEventTime,
  notificationFrequency,
  setNotificationFrequency,
  notificationInterval,
  setNotificationInterval,
  notificationStartTime,
  setNotificationStartTime,
  file,
  setFile,
  fileInputRef,
  fileName,
  showIcons,
  toggleEditMode,
  invitees,
  setInvitees,
  deleteEvent,
}) => {
  const [inviteeEmail, setInviteeEmail] = useState("");

  const handleAddInvitee = async () => {
    if (inviteeEmail && !invitees.includes(inviteeEmail)) {
      try {
        const response = await axios.get(`api/events/email/${inviteeEmail}`);
        if (response.data) {
          setInvitees([...invitees, inviteeEmail]);
          setInviteeEmail("");
          setErrors((prev) => ({ ...prev, inviteeEmail: "" }));
        } else {
          message.error("Bu e-posta adresine ait kullanıcı bulunamadı");
          setErrors((prev) => ({
            ...prev,
            inviteeEmail: "*",
          }));
        }
      } catch (error) {
        message.error("Bu e-posta adresine ait kullanıcı bulunamadı");
        setErrors((prev) => ({
          ...prev,
          inviteeEmail: "*",
        }));
      }
    }
  };
  const handleSave = () => {
    if (inviteeEmail) {
      message.error(
        "Lütfen e-posta alanındaki davetliyi ekleyin veya alanı boşaltın"
      );
      setErrors((prev) => ({
        ...prev,
        inviteeEmail: "*",
      }));
    } else {
      handleSubmit();
    }
  };

  const onFileDelete = () => {
    handleFileDelete();
    updateFileName("");
  };
  const handleRemoveInvitee = (emailToRemove) => {
    setInvitees(invitees.filter((invitee) => invitee !== emailToRemove));
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
        message.error(
          "Dosya boyutu çok büyük, lütfen daha küçük bir dosya seçin"
        );
        setErrors((prev) => ({
          ...prev,
          file: "*",
        }));
      } else {
        message.error("Dosya yüklenirken hata oluştu, lütfen tekrar deneyin");
        setErrors((prev) => ({
          ...prev,
          file: "*",
        }));
      }
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} locale={trLocale}>
      <div className="event-form-create">
        <div className="event-form-create-view">
          {showIcons && (
            <div className="event-form-create-icons">
              <FaEdit
                onClick={toggleEditMode}
                className="event-form-create-icons-edit"
              />
              <FaTrash
                onClick={deleteEvent}
                className="event-form-create-icons-trash"
              />
            </div>
          )}
          <div className="event-form-create-input-group">
            <p>
              <strong className="event-form-create-title">
                Etkinlik İsmi<p>:</p>
              </strong>
              <TextField
                type="string"
                value={eventName}
                onChange={(e) => {
                  setEventName(e.target.value);
                  setErrors((prev) => ({ ...prev, eventName: "" }));
                }}
                error={!!errors.eventName}
              />
              {!!errors.eventName && (
                <span className="event-form-create-error-message">
                  {errors.eventName}
                </span>
              )}
            </p>
          </div>

          <div className="event-form-create-input-group">
            <p>
              <strong className="event-form-create-title">
                Etkinlik Tarihi<p>:</p>
              </strong>
              <DateTimePicker
                value={eventTime}
                onChange={(newValue) => {
                  setEventTime(new Date(newValue));
                  setErrors((prev) => ({
                    ...prev,
                    eventTime: "",
                    eventTimeError: "",
                  }));
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    error={!!errors.eventTime || !!errors.eventTimeError}
                  />
                )}
                ampm={false}
              />
              {(!!errors.eventTime || !!errors.eventTimeError) && (
                <span className="event-form-create-error-message">
                  {errors.eventTime || errors.eventTimeError}
                </span>
              )}
            </p>
          </div>
          <div className="event-form-create-input-group">
            <p>
              <strong className="event-form-create-title">
                Bildirim Sıklığı<p>:</p>
              </strong>
              <TextField
                type="number"
                value={notificationFrequency}
                onChange={(e) => {
                  setNotificationFrequency(e.target.value);
                  setErrors((prev) => ({
                    ...prev,
                    notificationFrequency: "",
                  }));
                }}
                inputProps={{ min: 1 }}
                error={!!errors.notificationFrequency}
                className="event-form-create-input-select-left"
              />
              {"      "}
              <TextField
                select
                value={notificationInterval}
                onChange={(e) => {
                  setNotificationInterval(e.target.value);
                  setErrors((prev) => ({
                    ...prev,
                    notificationInterval: "",
                  }));
                }}
                SelectProps={{ native: true }}
                error={!!errors.notificationInterval}
                className="event-form-create-input-select-right"
              >
                <option value="" disabled>
                  Seç
                </option>
                <option value="minutes">Dakika</option>
                <option value="hours">Saat</option>
                <option value="days">Gün</option>
                <option value="weeks">Hafta</option>
                <option value="months">Ay</option>
                <option value="years">Yıl</option>
              </TextField>
              {(errors.notificationInterval ||
                errors.notificationFrequency) && (
                <span className="event-form-create-error-message">
                  {errors.notificationInterval || errors.notificationFrequency}
                </span>
              )}
            </p>
          </div>
          <div className="event-form-create-input-group">
            <p>
              <strong className="event-form-create-title">
                Bildirim Başlangıç Tarihi<p>:</p>
              </strong>
              <DateTimePicker
                value={notificationStartTime}
                onChange={(newValue) => {
                  setNotificationStartTime(new Date(newValue));
                  setErrors((prev) => ({
                    ...prev,
                    notificationStartTime: "",
                    notificationTimeError: "",
                  }));
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    error={
                      !!errors.notificationStartTime ||
                      !!errors.notificationTimeError
                    }
                  />
                )}
                ampm={false}
              />
              {(!!errors.notificationStartTime ||
                !!errors.notificationTimeError) && (
                <span className="event-form-create-error-message">
                  {errors.notificationStartTime || errors.notificationTimeError}
                </span>
              )}
            </p>
          </div>
          <div className="event-form-create-input-group">
            <p>
              <strong className="event-form-create-title">
                Davetliler<p>:</p>
              </strong>
              <TextField
                type="email"
                placeholder="Davetli e-posta"
                value={inviteeEmail}
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
              />
              <button
                className="event-form-create-handle-add-invitee-button"
                onClick={handleAddInvitee}
              >
                <PersonAddIcon className="event-form-create-personal-add-icon" />
              </button>
              {!!errors.inviteeEmail && (
                <span className="event-form-create-error-message">
                  {errors.inviteeEmail}
                </span>
              )}
              {invitees.length > 0 && (
                <ul className="event-form-create-invitees-list">
                  {invitees.map((invitee) => (
                    <li key={invitee}>
                      {invitee}
                      <button
                        className="event-form-create-close-icon-button"
                        onClick={() => handleRemoveInvitee(invitee)}
                      >
                        <CloseIcon className="event-form-create-close-icon" />
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </p>
          </div>

          <div className="event-form-create-input-group">
            <div className="event-form-create-file-upload">
              <strong className="event-form-create-title">
                Dosya<p>:</p>
              </strong>
              {fileName && (
                <div className="event-form-create-input-file">
                  <span>{truncateFileName(fileName, 11)}</span>
                  <button
                    className="event-form-create-close-icon-button"
                    onClick={onFileDelete}
                  >
                    <CloseIcon className="event-form-create-close-icon" />
                  </button>
                </div>
              )}
              <input
                type="file"
                ref={fileInputRef}
                onChange={(e) => {
                  const file = e.target.files[0];
                  setFile(file);
                  handleFileUpload(file);
                  setErrors((prev) => ({ ...prev, file: "" }));
                }}
                className={errors.file ? "input-error" : ""}
              />
              {file && (
                <button
                  className="event-form-create-input-close-button"
                  onClick={handleInputFileDelete}
                >
                  <CloseIcon className="event-form-create-input-close-icon" />
                </button>
              )}
              {!!errors.file && (
                <span className="event-form-create-error-message">
                  {errors.file}
                </span>
              )}
            </div>
          </div>
          <div className="event-form-create-button-container">
            <button onClick={handleSave}>Kaydet</button>
          </div>
        </div>
      </div>
    </LocalizationProvider>
  );
};

export default EventFormCreate;

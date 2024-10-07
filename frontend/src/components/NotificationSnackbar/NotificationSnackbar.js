import React from "react";
import Snackbar from "@mui/material/Snackbar";
import Alert from "@mui/material/Alert";
import IconButton from "@mui/material/IconButton";
import CloseIcon from "@mui/icons-material/Close";
import Button from "@mui/material/Button";
import stopNotifications from "../StopNotification/StopNotification";
import { useNotification } from "../../NotificationContext";

const NotificationSnackbar = ({ open, message, eventId, handleClose }) => {
  const { stopNotification } = useNotification();

  const handleButtonClick = () => {
    stopNotifications(eventId, stopNotification);
    handleClose();
  };

  return (
    <Snackbar
      open={open}
      autoHideDuration={10000}
      anchorOrigin={{ vertical: "top", horizontal: "center" }}
    >
      <Alert
        onClose={handleClose}
        severity="info"
        sx={{ width: "100%" }}
        action={
          <>
            <Button color="inherit" size="small" onClick={handleButtonClick}>
              Evet
            </Button>
            <IconButton
              size="small"
              aria-label="close"
              color="inherit"
              onClick={handleClose}
            >
              <CloseIcon fontSize="small" />
            </IconButton>
          </>
        }
      >
        {message}
        <br />
        Bildirimi iptal etmek isterseniz evet'e tıklayınız.
      </Alert>
    </Snackbar>
  );
};

export default NotificationSnackbar;

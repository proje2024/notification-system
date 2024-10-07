import React from "react";
import { useNavigate } from "react-router-dom";
import { IconButton } from "@mui/material";
import ExitToAppIcon from "@mui/icons-material/ExitToApp";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import { useAuth } from "../../AuthContext";

const Navigation = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  return (
    <>
      {isAuthenticated && (
        <>
          <IconButton color="inherit" onClick={() => navigate("/calendar")}>
            <CalendarMonthIcon />
          </IconButton>
          <IconButton color="inherit" onClick={logout}>
            <ExitToAppIcon />
          </IconButton>
        </>
      )}
    </>
  );
};

export default Navigation;

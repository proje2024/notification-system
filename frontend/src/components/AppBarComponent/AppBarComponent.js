import React from "react";
import { useNavigate } from "react-router-dom";
import { IconButton } from "@mui/material";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Navigation from "../Navigation/Navigation";
import HomeIcon from "@mui/icons-material/Home";
import { useLocation } from "react-router-dom";
import "./AppBarComponent.css";

const AppBarComponent = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const showHomeIcon = location.pathname !== "/home";
  return (
    <AppBar position="fixed" className="app-bar">
      <Toolbar className="styled-toolbar">
        {showHomeIcon && (
          <IconButton color="inherit" onClick={() => navigate("/home")}>
            <HomeIcon />
          </IconButton>
        )}
        <Toolbar className="styled-right-toolbar">
          <Navigation />
        </Toolbar>
      </Toolbar>
    </AppBar>
  );
};

export default AppBarComponent;

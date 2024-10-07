import React, { useEffect } from "react";
import { useTheme } from "@mui/material/styles";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider, useAuth } from "./AuthContext";
import { NotificationProvider } from "./NotificationContext";
import PrivateRoute from "./PrivateRoute";
import AppBarComponent from "./components/AppBarComponent/AppBarComponent";
import Home from "./pages/Home/Home";
import NotFound from "./components/NotFound/NotFound";
import Calendar from "./pages/Calendar/Calendar";
import Notification from "./components/Notification/Notification";
import "./index.css";

function App() {
  const theme = useTheme();

  useEffect(() => {
    if (theme.palette.mode === "dark") {
      document.body.classList.add("dark-mode");
    } else {
      document.body.classList.remove("dark-mode");
    }
  }, [theme.palette.mode]);

  return (
    <AuthProvider>
      <NotificationProvider>
        <Router>
          <AppContent />
        </Router>
      </NotificationProvider>
    </AuthProvider>
  );
}

const AppContent = () => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div>Yükleniyor...</div>;
  }

  return (
    <>
      <AppBarComponent />
      {isAuthenticated && <Notification />}
      <Routes>
        <Route element={<PrivateRoute />}>
          <Route path="/home" element={<Home />} />
          <Route path="/calendar" element={<Calendar />} />
          <Route path="/" element={<Home />} />
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
};

export default App;

import React, { createContext, useState, useEffect, useContext } from "react";
import axios from "axios";
import jwtDecode from "jwt-decode";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  const redirectToKeycloak = () => {
    const clientId = process.env.REACT_APP_KEYCLOAK_CLIENT;
    const currentPath = window.location.pathname;
    const currentSearch = window.location.search;

    if (currentSearch) {
      localStorage.setItem("savedQueryParams", currentSearch);
    }
    const redirectUri = encodeURIComponent(
      `${process.env.REACT_APP_REDIRECT_URI}${currentPath}`
    );

    const authorizationUrl = `${process.env.REACT_APP_KEYCLOAK_HOST}:${process.env.REACT_APP_KEYCLOAK_PORT}/realms/${process.env.REACT_APP_KEYCLOAK_REALM}/protocol/openid-connect/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=openid`;

    window.location.href = authorizationUrl;
  };

  const checkTokenValidity = (token) => {
    try {
      const decodedToken = jwtDecode(token);
      const currentTime = Date.now() / 1000;

      if (decodedToken.exp < currentTime) {
        return false;
      }
      return true;
    } catch (error) {
      console.error("Geçersiz token:", error);
      return false;
    }
  };

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (token) {
      const isValid = checkTokenValidity(token);
      if (isValid) {
        setIsAuthenticated(true);
        setLoading(false);
      } else {
        localStorage.removeItem("token");
        localStorage.removeItem("refresh_token");
        redirectToKeycloak();
      }
    } else {
      const urlParams = new URLSearchParams(window.location.search);
      const authorizationCode = urlParams.get("code");
      if (authorizationCode) {
        fetchTokenFromBackend(authorizationCode)
          .then(() => {
            setLoading(false);
          })
          .catch(() => {
            redirectToKeycloak();
          });
      } else {
        redirectToKeycloak();
      }
    }
  }, []);

  const fetchTokenFromBackend = async (authorizationCode) => {
    const currentPath = window.location.pathname;
    try {
      const response = await axios.post(
        `/token`,
        `code=${authorizationCode}&currentPath=${currentPath}`,
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      );

      const { access_token, refresh_token, id_token } = response.data;
      if (access_token && refresh_token && id_token) {
        localStorage.setItem("token", access_token);
        localStorage.setItem("refresh_token", refresh_token);
        localStorage.setItem("id_token", id_token);
        setIsAuthenticated(true);

        const savedQueryParams = localStorage.getItem("savedQueryParams");
        if (savedQueryParams) {
          localStorage.removeItem("savedQueryParams");
          window.history.replaceState(
            null,
            "",
            `${currentPath}${savedQueryParams}`
          );
        }
      } else {
        redirectToKeycloak();
      }
    } catch (error) {
      setIsAuthenticated(false);
      redirectToKeycloak();
    }
  };

  const refreshToken = async () => {
    const refreshToken = localStorage.getItem("refresh_token");
    if (!refreshToken) {
      setIsAuthenticated(false);
      redirectToKeycloak();
      return;
    }

    try {
      const response = await axios.post(
        `/token/refresh`,
        `refreshToken=${refreshToken}`,
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      );

      const { access_token, refresh_token: newRefreshToken } = response.data;
      if (access_token && newRefreshToken) {
        localStorage.setItem("token", access_token);
        localStorage.setItem("refresh_token", newRefreshToken);
        setIsAuthenticated(true);
      } else {
        throw new Error("Token yenilenemedi");
      }
    } catch (error) {
      console.error("Token yenilenemedi:", error);
      setIsAuthenticated(false);
      redirectToKeycloak();
    }
  };

  useEffect(() => {
    const intervalId = setInterval(() => {
      refreshToken();
    }, Number(process.env.REACT_APP_REFLESH_TOKEN_TIME));

    return () => clearInterval(intervalId);
  }, []);

  const logout = async () => {
    try {
      const idToken = localStorage.getItem("id_token");
      const currentPath = window.location.pathname;
      const postLogoutRedirectUri = encodeURIComponent(
        `${process.env.REACT_APP_REDIRECT_URI}${currentPath}`
      );
      window.location.href = `${process.env.REACT_APP_KEYCLOAK_HOST}:${process.env.REACT_APP_KEYCLOAK_PORT}/realms/${process.env.REACT_APP_KEYCLOAK_REALM}/protocol/openid-connect/logout?id_token_hint=${idToken}&post_logout_redirect_uri=${postLogoutRedirectUri}`;

      localStorage.removeItem("token");
      localStorage.removeItem("refresh_token");
      localStorage.removeItem("id_token");
      setIsAuthenticated(false);
    } catch (error) {
      console.error("Çıkış yapılamadı. Lütfen tekrar deneyiniz", error);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        redirectToKeycloak,
        logout,
        loading,
        fetchTokenFromBackend,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

import axios from "axios";
import { message } from "antd";

const stopNotifications = async (eventId, stopNotification) => {
  try {
    const response = await axios.post(
      `/api/events/${eventId}/stop-notifications`
    );

    if (response.status === 200) {
      message.success("Bildirim başarıyla iptal edildi");
      stopNotification(eventId);
    } else {
      message.error("Bildirim iptal edilirken hata oluştu");
    }
  } catch (error) {
    message.error("Bildirim iptal edilirken hata oluştu");
    console.error("Bildirim iptal edilirken hata oluştu:", error);
  }
};

export default stopNotifications;

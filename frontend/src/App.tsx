import { useEffect, useState } from "react";
import { fetchHealth, type HealthStatus } from "./api/health";
import "./App.css";

type DisplayStatus = HealthStatus | "loading" | "error";

function App() {
  const [health, setHealth] = useState<DisplayStatus>("loading");

  useEffect(() => {
    fetchHealth()
      .then(setHealth)
      .catch(() => setHealth("error"));
  }, []);

  return (
    <main>
      <h1>Shop coming soon</h1>
      <p className={`status status--${health.toLowerCase()}`}>Backend: {health}</p>
    </main>
  );
}

export default App;

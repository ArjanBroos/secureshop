import { useEffect, useState } from "react";
import "./App.css";

type HealthStatus = "loading" | "UP" | "DOWN" | "error";

function App() {
  const [health, setHealth] = useState<HealthStatus>("loading");

  useEffect(() => {
    fetch("/actuator/health")
      .then((res) => res.json())
      .then((data) => setHealth(data.status === "UP" ? "UP" : "DOWN"))
      .catch(() => setHealth("error"));
  }, []);

  return (
    <main>
      <h1>Shop coming soon</h1>
      <p className={`status status--${health.toLowerCase()}`}>
        Backend: {health}
      </p>
    </main>
  );
}

export default App;

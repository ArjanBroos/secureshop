export type HealthStatus = "UP" | "DOWN";

const BASE_URL = import.meta.env.VITE_API_URL ?? "";

export async function fetchHealth(): Promise<HealthStatus> {
  const response = await fetch(`${BASE_URL}/actuator/health`);
  if (!response.ok) {
    return "DOWN";
  }
  const data = await response.json();
  return data.status === "UP" ? "UP" : "DOWN";
}

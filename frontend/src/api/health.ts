export type HealthStatus = "UP" | "DOWN";

export async function fetchHealth(): Promise<HealthStatus> {
  const response = await fetch("/actuator/health");
  if (!response.ok) {
    return "DOWN";
  }
  const data = await response.json();
  return data.status === "UP" ? "UP" : "DOWN";
}

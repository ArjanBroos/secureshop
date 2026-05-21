import { render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import App from "./App";
import { fetchHealth } from "./api/health";

vi.mock("./api/health");

describe("App", () => {
  it("renders the coming soon heading", () => {
    vi.mocked(fetchHealth).mockResolvedValue("UP");

    render(<App />);

    expect(screen.getByText("Shop coming soon")).toBeInTheDocument();
  });

  it("shows UP status when backend is healthy", async () => {
    vi.mocked(fetchHealth).mockResolvedValue("UP");

    render(<App />);

    await waitFor(() => expect(screen.getByText("Backend: UP")).toBeInTheDocument());
  });

  it("shows error status when backend is unreachable", async () => {
    vi.mocked(fetchHealth).mockRejectedValue(new Error("Network error"));

    render(<App />);

    await waitFor(() => expect(screen.getByText("Backend: error")).toBeInTheDocument());
  });
});

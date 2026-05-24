import { render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import App from "./App";
import { getProducts } from "./api/products";

vi.mock("./api/products");

describe("App", () => {
  it("renders the catalog page", async () => {
    vi.mocked(getProducts).mockResolvedValue({
      items: [
        {
          id: "1",
          name: "Widget",
          description: "A useful widget",
          priceCents: 999,
          currency: "EUR",
          imageUrl: "https://example.com/widget.png",
        },
      ],
      totalItems: 1,
      page: 0,
      size: 20,
    });

    render(<App />);

    await waitFor(() => expect(screen.getByText("Products")).toBeInTheDocument());
  });
});

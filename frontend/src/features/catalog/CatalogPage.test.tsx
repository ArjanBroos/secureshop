import { render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { CatalogPage } from "./CatalogPage";
import { getProducts } from "../../api/products";

vi.mock("../../api/products");

describe("CatalogPage", () => {
  it("shows product names after loading", async () => {
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

    render(<CatalogPage />);

    await waitFor(() => expect(screen.getByText("Widget")).toBeInTheDocument());
  });

  it("shows an error message when the API fails", async () => {
    vi.mocked(getProducts).mockRejectedValue(new Error("Network error"));

    render(<CatalogPage />);

    await waitFor(() => expect(screen.getByText(/failed to load/i)).toBeInTheDocument());
  });
});

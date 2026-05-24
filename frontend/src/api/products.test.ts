import { afterEach, describe, expect, it, vi } from "vitest";
import { getProductById, getProducts } from "./products";

afterEach(() => {
  vi.restoreAllMocks();
});

describe("getProducts", () => {
  it("returns a page of products from the backend response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () =>
          Promise.resolve({
            items: [
              {
                id: "abc",
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
          }),
      }),
    );

    const result = await getProducts();

    expect(result.items).toHaveLength(1);
    expect(result.items[0].name).toBe("Widget");
    expect(result.items[0].priceCents).toBe(999);
    expect(result.totalItems).toBe(1);
  });

  it("throws when the response is not ok", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 500 }));

    await expect(getProducts()).rejects.toThrow("500");
  });

  it("throws when the network fails", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("Network error")));

    await expect(getProducts()).rejects.toThrow();
  });
});

describe("getProductById", () => {
  it("returns the product when found", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () =>
          Promise.resolve({
            id: "abc",
            name: "Widget",
            description: "A useful widget",
            priceCents: 999,
            currency: "EUR",
            imageUrl: "https://example.com/widget.png",
          }),
      }),
    );

    const result = await getProductById("abc");

    expect(result.id).toBe("abc");
    expect(result.name).toBe("Widget");
  });

  it("throws when the product is not found", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 404 }));

    await expect(getProductById("unknown")).rejects.toThrow("404");
  });
});

import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchHealth } from "./health";

afterEach(() => {
  vi.restoreAllMocks();
});

describe("fetchHealth", () => {
  it("returns UP when backend reports UP", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ status: "UP" }),
      }),
    );

    expect(await fetchHealth()).toBe("UP");
  });

  it("returns DOWN when backend reports non-UP status", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ status: "DOWN" }),
      }),
    );

    expect(await fetchHealth()).toBe("DOWN");
  });

  it("returns DOWN when response is not ok", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        json: () => Promise.resolve({}),
      }),
    );

    expect(await fetchHealth()).toBe("DOWN");
  });

  it("throws when the network fails", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("Network error")));

    await expect(fetchHealth()).rejects.toThrow();
  });
});

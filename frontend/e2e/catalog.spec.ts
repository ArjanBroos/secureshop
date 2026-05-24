import { test, expect } from "@playwright/test";

test("catalog page shows products", { tag: "@smoke" }, async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Products" })).toBeVisible();
  await expect(page.getByRole("list")).not.toBeEmpty();
});

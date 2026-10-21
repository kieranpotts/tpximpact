import { expect, test } from "@playwright/test";
import { publicBaseUrl } from "./urls.js";

test.describe("Q4 — Containerised", () => {
  test("the web client serves its home page", async ({ page }) => {
    await page.goto("/");

    await expect(page.getByRole("heading", { level: 1 })).toHaveText(
      "URL shortener",
    );
  });

  test("the API service is reachable at the public base URL", async ({
    request,
  }) => {
    const response = await request.get(publicBaseUrl);

    expect(response.status()).toBe(200);
  });
});

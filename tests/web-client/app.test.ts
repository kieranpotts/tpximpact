import * as cheerio from "cheerio";
import request from "supertest";
import { describe, expect, it } from "vitest";
import { createApp } from "../../src/web-client/src/app.js";

describe("the home page", () => {
  it("is the placeholder page", async () => {
    const response = await request(createApp()).get("/");

    expect(response.status).toBe(200);
    expect(response.type).toBe("text/html");
    const $ = cheerio.load(response.text);
    expect($("h1").text()).toBe("URL shortener");
  });
});

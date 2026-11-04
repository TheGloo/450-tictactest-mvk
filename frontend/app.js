// Embeds the web console and shows both versions. The URL of the web console comes
// from config.js (window.TICTACTEST_CONFIG), never from this file.
const config = window.TICTACTEST_CONFIG;

document.getElementById("terminal").src = config.webConsoleUrl;

async function show(elementId, url, read) {
  const element = document.getElementById(elementId);
  try {
    const response = await fetch(url, { cache: "no-store" });
    if (!response.ok) throw new Error(response.status);
    element.textContent = await read(response);
  } catch (error) {
    element.textContent = "unbekannt";
    console.warn(`version from ${url}:`, error);
  }
}

// frontend: version.json is part of the release archive
show("frontend-version", "version.json", async (r) => (await r.json()).version);
// backend: the web console answers /version with the version of the running jar
show("backend-version", new URL("version", config.webConsoleUrl), async (r) => (await r.text()).trim());
